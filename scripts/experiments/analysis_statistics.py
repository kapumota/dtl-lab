#!/usr/bin/env python3
"""Funciones estadísticas deterministas para el análisis de la Fase 8D."""

from __future__ import annotations

import hashlib
import math
import random
from collections.abc import Iterable
from statistics import fmean, median, stdev
from typing import Any


def numeric_values(values: Iterable[Any]) -> list[float]:
    """Conserva valores numéricos finitos y descarta booleanos."""
    result: list[float] = []
    for value in values:
        if isinstance(value, bool) or not isinstance(value, (int, float)):
            continue
        number = float(value)
        if math.isfinite(number):
            result.append(number)
    return result


def percentile(values: Iterable[Any], probability: float) -> float | None:
    """Calcula un percentil con interpolación lineal."""
    numbers = sorted(numeric_values(values))
    if not numbers:
        return None
    if not 0.0 <= probability <= 1.0:
        raise ValueError("La probabilidad debe estar entre cero y uno.")
    if len(numbers) == 1:
        return numbers[0]
    position = probability * (len(numbers) - 1)
    lower_index = math.floor(position)
    upper_index = math.ceil(position)
    if lower_index == upper_index:
        return numbers[lower_index]
    fraction = position - lower_index
    return (
        numbers[lower_index] * (1.0 - fraction)
        + numbers[upper_index] * fraction
    )


def deterministic_seed(key: str) -> int:
    """Deriva una seed estable desde una clave textual."""
    digest = hashlib.sha256(key.encode("utf-8")).hexdigest()
    return int(digest[:16], 16)


def bootstrap_median_interval(
    values: Iterable[Any],
    *,
    resamples: int,
    key: str,
) -> tuple[float | None, float | None]:
    """Calcula un intervalo bootstrap determinista del 95 por ciento."""
    numbers = numeric_values(values)
    if not numbers:
        return None, None
    if resamples <= 0:
        raise ValueError("La cantidad de remuestras debe ser positiva.")
    if len(numbers) == 1:
        return numbers[0], numbers[0]

    generator = random.Random(deterministic_seed(key))
    medians: list[float] = []
    for _ in range(resamples):
        sample = [
            numbers[generator.randrange(len(numbers))]
            for _ in range(len(numbers))
        ]
        medians.append(float(median(sample)))
    return percentile(medians, 0.025), percentile(medians, 0.975)


def summarize_numeric(
    values: Iterable[Any],
    *,
    resamples: int,
    key: str,
) -> dict[str, float | int | None]:
    """Resume ubicación, dispersión e intervalo bootstrap."""
    numbers = numeric_values(values)
    if not numbers:
        return {
            "n": 0,
            "median": None,
            "q1": None,
            "q3": None,
            "iqr": None,
            "minimum": None,
            "maximum": None,
            "mean": None,
            "coefficient_of_variation": None,
            "bootstrap_95_lower": None,
            "bootstrap_95_upper": None,
        }

    q1 = percentile(numbers, 0.25)
    q3 = percentile(numbers, 0.75)
    mean_value = fmean(numbers)
    variation = None
    if len(numbers) > 1 and mean_value != 0:
        variation = stdev(numbers) / abs(mean_value)
    lower, upper = bootstrap_median_interval(
        numbers,
        resamples=resamples,
        key=key,
    )
    return {
        "n": len(numbers),
        "median": float(median(numbers)),
        "q1": q1,
        "q3": q3,
        "iqr": None if q1 is None or q3 is None else q3 - q1,
        "minimum": min(numbers),
        "maximum": max(numbers),
        "mean": mean_value,
        "coefficient_of_variation": variation,
        "bootstrap_95_lower": lower,
        "bootstrap_95_upper": upper,
    }


def wilson_interval(
    successes: int,
    total: int,
    *,
    z_score: float = 1.959963984540054,
) -> tuple[float | None, float | None]:
    """Calcula el intervalo Wilson del 95 por ciento."""
    if total < 0 or successes < 0 or successes > total:
        raise ValueError("Los conteos para Wilson son inválidos.")
    if total == 0:
        return None, None
    proportion = successes / total
    denominator = 1.0 + (z_score * z_score) / total
    center = (
        proportion + (z_score * z_score) / (2.0 * total)
    ) / denominator
    margin = (
        z_score
        * math.sqrt(
            proportion * (1.0 - proportion) / total
            + (z_score * z_score) / (4.0 * total * total)
        )
        / denominator
    )
    return max(0.0, center - margin), min(1.0, center + margin)


def average_ranks(values: list[float]) -> list[float]:
    """Asigna rangos promedio y conserva empates."""
    indexed = sorted(enumerate(values), key=lambda item: item[1])
    ranks = [0.0] * len(values)
    cursor = 0
    while cursor < len(indexed):
        end = cursor + 1
        while end < len(indexed) and indexed[end][1] == indexed[cursor][1]:
            end += 1
        average = (cursor + 1 + end) / 2.0
        for position in range(cursor, end):
            ranks[indexed[position][0]] = average
        cursor = end
    return ranks


def pearson_correlation(left: list[float], right: list[float]) -> float | None:
    """Calcula correlación de Pearson para dos vectores."""
    if len(left) != len(right) or len(left) < 2:
        return None
    left_mean = fmean(left)
    right_mean = fmean(right)
    numerator = sum(
        (left_value - left_mean) * (right_value - right_mean)
        for left_value, right_value in zip(left, right, strict=True)
    )
    left_square = sum((value - left_mean) ** 2 for value in left)
    right_square = sum((value - right_mean) ** 2 for value in right)
    denominator = math.sqrt(left_square * right_square)
    if denominator == 0:
        return None
    return numerator / denominator


def spearman_correlation(
    left: Iterable[Any],
    right: Iterable[Any],
) -> float | None:
    """Calcula correlación de Spearman con rangos promedio."""
    pairs: list[tuple[float, float]] = []
    for left_value, right_value in zip(left, right, strict=True):
        left_numbers = numeric_values([left_value])
        right_numbers = numeric_values([right_value])
        if left_numbers and right_numbers:
            pairs.append((left_numbers[0], right_numbers[0]))
    if len(pairs) < 2:
        return None
    left_ranks = average_ranks([pair[0] for pair in pairs])
    right_ranks = average_ranks([pair[1] for pair in pairs])
    return pearson_correlation(left_ranks, right_ranks)
