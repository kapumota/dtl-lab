#!/usr/bin/env python3
"""Generadores SVG sin dependencias externas para la Fase 8D."""

from __future__ import annotations

import html
import math
from pathlib import Path
from typing import Any


def escape(value: Any) -> str:
    """Escapa texto para incluirlo en SVG."""
    return html.escape(str(value), quote=True)


def format_number(value: float) -> str:
    """Formatea números de ejes de manera compacta."""
    absolute = abs(value)
    if absolute >= 1_000_000:
        return f"{value / 1_000_000:.1f}M"
    if absolute >= 1_000:
        return f"{value / 1_000:.1f}k"
    if absolute >= 10:
        return f"{value:.0f}"
    return f"{value:.2f}".rstrip("0").rstrip(".")


def svg_document(
    title: str,
    body: list[str],
    *,
    width: int = 960,
    height: int = 600,
) -> str:
    """Construye un documento SVG completo."""
    lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        (
            f'<svg xmlns="http://www.w3.org/2000/svg" '
            f'width="{width}" height="{height}" '
            f'viewBox="0 0 {width} {height}">'
        ),
        f"  <title>{escape(title)}</title>",
        "  <rect width=\"100%\" height=\"100%\" fill=\"white\"/>",
        "  <style>",
        "    text { font-family: sans-serif; fill: #111; }",
        "    .title { font-size: 24px; font-weight: 700; }",
        "    .axis { stroke: #111; stroke-width: 1.5; }",
        "    .grid { stroke: #bbb; stroke-width: 1; stroke-dasharray: 4 4; }",
        "    .label { font-size: 13px; }",
        "    .small { font-size: 11px; }",
        "    .series-a { stroke: #111; fill: #111; }",
        "    .series-b { stroke: #555; fill: #555; }",
        "    .series-c { stroke: #999; fill: #999; }",
        "    .box { fill: #f7f7f7; stroke: #111; stroke-width: 1.5; }",
        "  </style>",
        *body,
        "</svg>",
        "",
    ]
    return "\n".join(lines)


def write_svg(path: Path, content: str) -> None:
    """Escribe un SVG con finales de línea reproducibles."""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8", newline="\n")


def render_message(path: Path, title: str, message: str) -> None:
    """Genera una figura informativa cuando faltan datos."""
    body = [
        f'  <text x="480" y="70" text-anchor="middle" class="title">{escape(title)}</text>',
        f'  <text x="480" y="300" text-anchor="middle" class="label">{escape(message)}</text>',
    ]
    write_svg(path, svg_document(title, body))


def render_architecture(path: Path) -> None:
    """Genera la arquitectura de evidencia del Paper 1."""
    title = "Arquitectura de evidencia"
    boxes = [
        (70, 180, 170, 90, "Java", "Trazas concretas"),
        (290, 180, 170, 90, "Abstracción", "Estados y acciones"),
        (510, 110, 170, 90, "TLA+", "Propiedades y replay"),
        (510, 250, 170, 90, "Alloy", "Assertions y mutantes"),
        (730, 180, 170, 90, "Fase 8D", "Tablas y figuras"),
    ]
    body = [
        f'  <text x="480" y="55" text-anchor="middle" class="title">{escape(title)}</text>'
    ]
    for x, y, width, height, heading, subtitle in boxes:
        body.extend(
            [
                f'  <rect x="{x}" y="{y}" width="{width}" height="{height}" rx="8" class="box"/>',
                f'  <text x="{x + width / 2}" y="{y + 35}" text-anchor="middle" class="label" font-weight="700">{escape(heading)}</text>',
                f'  <text x="{x + width / 2}" y="{y + 60}" text-anchor="middle" class="small">{escape(subtitle)}</text>',
            ]
        )
    arrows = [
        (240, 225, 290, 225),
        (460, 210, 510, 155),
        (460, 240, 510, 295),
        (680, 155, 730, 210),
        (680, 295, 730, 240),
    ]
    for x1, y1, x2, y2 in arrows:
        body.append(
            f'  <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" class="axis"/>'
        )
        angle = math.atan2(y2 - y1, x2 - x1)
        for offset in (-0.45, 0.45):
            x3 = x2 - 12 * math.cos(angle + offset)
            y3 = y2 - 12 * math.sin(angle + offset)
            body.append(
                f'  <line x1="{x2}" y1="{y2}" x2="{x3:.1f}" y2="{y3:.1f}" class="axis"/>'
            )
    body.append(
        '  <text x="480" y="420" text-anchor="middle" class="label">Los resultados raw permanecen inmutables y los derivados conservan hashes de entrada.</text>'
    )
    write_svg(path, svg_document(title, body))


def render_flow(path: Path) -> None:
    """Genera el flujo del protocolo experimental."""
    title = "Flujo del protocolo experimental"
    labels = [
        "Protocolo congelado",
        "Plan de 1272 tareas",
        "Ejecución raw",
        "Validación de integridad",
        "Análisis estadístico",
        "Tablas y figuras",
    ]
    body = [
        f'  <text x="480" y="55" text-anchor="middle" class="title">{escape(title)}</text>'
    ]
    x_positions = [50, 200, 350, 500, 650, 800]
    for index, (x, label) in enumerate(zip(x_positions, labels, strict=True)):
        body.extend(
            [
                f'  <rect x="{x}" y="220" width="120" height="100" rx="8" class="box"/>',
                f'  <text x="{x + 60}" y="260" text-anchor="middle" class="small">{escape(label)}</text>',
            ]
        )
        if index < len(x_positions) - 1:
            x2 = x + 145
            body.extend(
                [
                    f'  <line x1="{x + 120}" y1="270" x2="{x2}" y2="270" class="axis"/>',
                    f'  <line x1="{x2}" y1="270" x2="{x2 - 10}" y2="264" class="axis"/>',
                    f'  <line x1="{x2}" y1="270" x2="{x2 - 10}" y2="276" class="axis"/>',
                ]
            )
    write_svg(path, svg_document(title, body))


def render_line_chart(
    path: Path,
    title: str,
    series: dict[str, list[tuple[float, float]]],
    *,
    x_label: str,
    y_label: str,
) -> None:
    """Genera un gráfico de líneas con una o más series."""
    clean_series = {
        name: sorted(
            [
                (float(x_value), float(y_value))
                for x_value, y_value in points
                if math.isfinite(float(x_value))
                and math.isfinite(float(y_value))
            ]
        )
        for name, points in series.items()
    }
    clean_series = {
        name: points for name, points in clean_series.items() if points
    }
    if not clean_series:
        render_message(path, title, "No existen observaciones numéricas suficientes.")
        return

    width = 960
    height = 600
    left = 100
    right = 70
    top = 90
    bottom = 100
    plot_width = width - left - right
    plot_height = height - top - bottom
    all_points = [point for points in clean_series.values() for point in points]
    x_values = [point[0] for point in all_points]
    y_values = [point[1] for point in all_points]
    x_min = min(x_values)
    x_max = max(x_values)
    y_min = min(0.0, min(y_values))
    y_max = max(y_values)
    if x_min == x_max:
        x_min -= 0.5
        x_max += 0.5
    if y_min == y_max:
        y_max = y_min + 1.0

    def x_position(value: float) -> float:
        return left + (value - x_min) / (x_max - x_min) * plot_width

    def y_position(value: float) -> float:
        return top + (y_max - value) / (y_max - y_min) * plot_height

    body = [
        f'  <text x="480" y="45" text-anchor="middle" class="title">{escape(title)}</text>',
        f'  <line x1="{left}" y1="{top}" x2="{left}" y2="{top + plot_height}" class="axis"/>',
        f'  <line x1="{left}" y1="{top + plot_height}" x2="{left + plot_width}" y2="{top + plot_height}" class="axis"/>',
    ]
    for index in range(6):
        value = y_min + (y_max - y_min) * index / 5
        y = y_position(value)
        body.extend(
            [
                f'  <line x1="{left}" y1="{y:.1f}" x2="{left + plot_width}" y2="{y:.1f}" class="grid"/>',
                f'  <text x="{left - 12}" y="{y + 4:.1f}" text-anchor="end" class="small">{escape(format_number(value))}</text>',
            ]
        )
    unique_x = sorted(set(x_values))
    for value in unique_x:
        x = x_position(value)
        body.append(
            f'  <text x="{x:.1f}" y="{top + plot_height + 28}" text-anchor="middle" class="small">{escape(format_number(value))}</text>'
        )
    body.extend(
        [
            f'  <text x="{left + plot_width / 2}" y="{height - 35}" text-anchor="middle" class="label">{escape(x_label)}</text>',
            f'  <text x="28" y="{top + plot_height / 2}" text-anchor="middle" class="label" transform="rotate(-90 28 {top + plot_height / 2})">{escape(y_label)}</text>',
        ]
    )

    classes = ["series-a", "series-b", "series-c"]
    for index, (name, points) in enumerate(sorted(clean_series.items())):
        css_class = classes[index % len(classes)]
        coordinates = " ".join(
            f"{x_position(x_value):.1f},{y_position(y_value):.1f}"
            for x_value, y_value in points
        )
        body.append(
            f'  <polyline points="{coordinates}" fill="none" class="{css_class}" stroke-width="2.5"/>'
        )
        for x_value, y_value in points:
            body.append(
                f'  <circle cx="{x_position(x_value):.1f}" cy="{y_position(y_value):.1f}" r="4" class="{css_class}"/>'
            )
        legend_y = 80 + index * 22
        body.extend(
            [
                f'  <line x1="{width - 190}" y1="{legend_y}" x2="{width - 155}" y2="{legend_y}" class="{css_class}" stroke-width="3"/>',
                f'  <text x="{width - 145}" y="{legend_y + 4}" class="small">{escape(name)}</text>',
            ]
        )
    write_svg(path, svg_document(title, body, width=width, height=height))


def render_bar_chart(
    path: Path,
    title: str,
    values: list[tuple[str, float]],
    *,
    y_label: str,
) -> None:
    """Genera un gráfico de barras verticales."""
    clean_values = [
        (str(label), float(value))
        for label, value in values
        if math.isfinite(float(value))
    ]
    if not clean_values:
        render_message(path, title, "No existen observaciones numéricas suficientes.")
        return

    width = 960
    height = 600
    left = 100
    right = 50
    top = 90
    bottom = 150
    plot_width = width - left - right
    plot_height = height - top - bottom
    maximum = max(value for _, value in clean_values)
    maximum = maximum if maximum > 0 else 1.0
    bar_width = plot_width / max(1, len(clean_values)) * 0.65
    gap = plot_width / max(1, len(clean_values))
    body = [
        f'  <text x="480" y="45" text-anchor="middle" class="title">{escape(title)}</text>',
        f'  <line x1="{left}" y1="{top}" x2="{left}" y2="{top + plot_height}" class="axis"/>',
        f'  <line x1="{left}" y1="{top + plot_height}" x2="{left + plot_width}" y2="{top + plot_height}" class="axis"/>',
    ]
    for index in range(6):
        value = maximum * index / 5
        y = top + plot_height - plot_height * index / 5
        body.extend(
            [
                f'  <line x1="{left}" y1="{y:.1f}" x2="{left + plot_width}" y2="{y:.1f}" class="grid"/>',
                f'  <text x="{left - 12}" y="{y + 4:.1f}" text-anchor="end" class="small">{escape(format_number(value))}</text>',
            ]
        )
    for index, (label, value) in enumerate(clean_values):
        x = left + index * gap + (gap - bar_width) / 2
        bar_height = value / maximum * plot_height
        y = top + plot_height - bar_height
        css_class = ["series-a", "series-b", "series-c"][index % 3]
        body.extend(
            [
                f'  <rect x="{x:.1f}" y="{y:.1f}" width="{bar_width:.1f}" height="{bar_height:.1f}" class="{css_class}" opacity="0.75"/>',
                f'  <text x="{x + bar_width / 2:.1f}" y="{y - 8:.1f}" text-anchor="middle" class="small">{escape(format_number(value))}</text>',
                f'  <text x="{x + bar_width / 2:.1f}" y="{top + plot_height + 20}" text-anchor="end" class="small" transform="rotate(-35 {x + bar_width / 2:.1f} {top + plot_height + 20})">{escape(label)}</text>',
            ]
        )
    body.append(
        f'  <text x="28" y="{top + plot_height / 2}" text-anchor="middle" class="label" transform="rotate(-90 28 {top + plot_height / 2})">{escape(y_label)}</text>'
    )
    write_svg(path, svg_document(title, body, width=width, height=height))
