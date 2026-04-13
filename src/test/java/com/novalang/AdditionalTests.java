package com.novalang;

import com.novalang.compiler.Translator;
import com.novalang.instructions.DivInstr;
import com.novalang.vm.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Additional Tests")
public class AdditionalTests {
    private static Path tempDir;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private Translator translator;
    private NovaVMFactory vmFactory;



    @BeforeAll
    static void init() throws IOException {
        tempDir = Files.createTempDirectory("novalang_integration_test");
    }

    @BeforeEach
    void setup() {
        translator = new Translator();
        vmFactory = new DefaultNovaVMFactory();



        // Capture stdout for print statements
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    private Path writeTempFile(String content) throws IOException {
        Path tempFile = tempDir.resolve("test_" + System.nanoTime() + ".nvl");
        return Files.writeString(tempFile, content);
    }

    private NovaVM runProgram(String nvlSource) throws Exception {
        Path tempFile = writeTempFile(nvlSource);
        translator.translate(tempFile);

        NovaVM vm = vmFactory.create(translator.program(), translator.labels());
        vm.run();
        return vm;
    }

    // helper method to create a ProgramContext for testing each instruction


    // VM Integration tests for instructions

    @Test
    @DisplayName("I5: Test Mov Instruction")
    void testMovInstruction() throws Exception {
        String nvlSource = """
                set r1 42
                mov r2 r1
                print r2
                halt
                """;
        NovaVM vm = runProgram(nvlSource);
        assertEquals(42, vm.getRegisters().get(2));
        assertTrue(outputStream.toString().contains("PRINT (r2): 42"));
    }

    @Test
    @DisplayName("I6: Test Mul Instruction")
    void testMulInstruction() throws Exception {
        String nvlSource = """
                set r1 8
                set r2 55
                mul r3 r1 r2
                print r3
                halt
                """;
        NovaVM vm = runProgram(nvlSource);
        assertEquals(440, vm.getRegisters().get(3));
        assertTrue(outputStream.toString().contains("PRINT (r3): 440"));
    }

    @Test
    @DisplayName("I7: DivInstr with invalid operands throws exception")
    // passing 2 parameters instead of 3 or non register operands should throw IllegalArgumentException
    void testDivInstrInvalidOperands() {
        assertThrows(IllegalArgumentException.class, () ->
                new DivInstr("L1", new Object[]{5, "r1", "r2"}));

        assertThrows(IllegalArgumentException.class, () ->
                new DivInstr("L2", new Object[]{"5", "r2"}));
    }



    // Edge case tests for error handling

    @Test
    @DisplayName("E1: Registers default to 0")
    void testDefaultRegisters() throws Exception {
        runProgram("""
                print r2
                print r31
                halt
                """);
        String output = outputStream.toString();
        assertTrue(output.contains("PRINT (r2): 0"));
        assertTrue(output.contains("PRINT (r31): 0"));
    }

    @Test
    @DisplayName("E2: RegisterManager throws on invalid get index")
    void testRegisterManagerInvalidGet() {
        var reg = new RegisterManager();
        assertThrows(IllegalArgumentException.class, () -> reg.get(32));
        assertThrows(IllegalArgumentException.class, () -> reg.get(-1));
    }

    @Test
    @DisplayName("E3: RegisterManager returns false on invalid  index")
    void testRegisterManagerInvalidSet() {
        var reg = new RegisterManager();
        assertFalse(reg.set(32, 15));
        assertFalse(reg.set(-1, 12));
    }

    @Test
    @DisplayName("E4: Subtract from itself results in 0")
    void testSubtractFromSelf() throws Exception {
        NovaVM vm = runProgram("""
                set r1 77
                sub r2 r1 r1
                print r2
                halt
                """);
        assertEquals(0, vm.getRegisters().get(2));
    }

    @Test
    @DisplayName("E5: Comments and empty lines are skipped")
    void testCommentsAndEmptyLines() throws Exception {
        NovaVM vm = runProgram("""
                # comment1
                ; comment2
                
                set r1 5
                ; comment3
                print r1
                halt
                """);
        assertEquals(5, vm.getRegisters().get(1));
    }

    // Validation tests for App

    @Test
    @DisplayName("A1: validate rejects non existent file")
    void testValidateRejectsNonExistent() {
        assertThrows(IllegalArgumentException.class, () -> App.validate("no_file.nvl"));
    }

    @Test
    @DisplayName("A2: validate accepts valid file")
    void testValidateAcceptsValidFile() throws IOException {
        Path file = App.validate("sum.nvl");
        assertNotNull(file);
        assertTrue(file.toString().endsWith(".nvl"));
    }
}
