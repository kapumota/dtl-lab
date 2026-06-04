package dltlab.transaction;

import java.math.BigInteger;

/** Representa una densidad economica medida en sats por vByte. */
public record FeeRate(long feeSats, long virtualSize) implements Comparable<FeeRate> {
    public FeeRate {
        if (virtualSize <= 0) {
            throw new IllegalArgumentException("El tamano virtual debe ser positivo.");
        }
    }

    public double satsPerVByte() {
        return (double) feeSats / (double) virtualSize;
    }

    public boolean isAtLeast(long minimumSatsPerVByte) {
        BigInteger left = BigInteger.valueOf(feeSats);
        BigInteger right = BigInteger.valueOf(minimumSatsPerVByte).multiply(BigInteger.valueOf(virtualSize));
        return left.compareTo(right) >= 0;
    }

    @Override
    public int compareTo(FeeRate other) {
        BigInteger left = BigInteger.valueOf(feeSats).multiply(BigInteger.valueOf(other.virtualSize));
        BigInteger right = BigInteger.valueOf(other.feeSats).multiply(BigInteger.valueOf(virtualSize));
        return left.compareTo(right);
    }
}
