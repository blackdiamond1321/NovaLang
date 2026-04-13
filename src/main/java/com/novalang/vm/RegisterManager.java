package com.novalang.vm;

import java.util.*;
import java.util.stream.Collectors;

// Utility class to manage 32 registers
public record RegisterManager(int[] registers) {
    public RegisterManager() {
        this(new int[32]); // Registers r0..r31, default value 0
    }

    // Validation for register index (Correctness)
    private boolean isValidIndex(int index) {
        return index >= 0 && index < registers.length;
    }

    // changed Optional<Integer> to int as for test I2 it returns Optional[25] instead of just int 25
    public int get(int index) {
        if (!isValidIndex(index)) {
            // throw an exception instead of returning Optional.empty() as Optional was changed to int
            throw new IllegalArgumentException("Index out of range: " + index);

        }
        return registers[index];
    }

    public boolean set(int index, int value) {
        if (!isValidIndex(index)) {
            return false;
        }
        registers[index] = value;
        return true;
    }

    // Helper for debugging/testing
    public String dumpRegisters() {
        return Arrays.stream(registers).mapToObj(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
    }
}