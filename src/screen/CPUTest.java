package screen;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CPUTest {

    private Chip8CPU target = new Chip8CPU();

    @Before
    public void init() {
        // clear all registers, memory etc
        target.initialize();
    }

    @Test
    public void testClear() { // 00E0
        // add pixel
        target.setGFXAtXY(15, 24);

        target.OP00E0();

        int[][] gfx = target.getGFX();

        // confirm pixel was removed
        boolean valid = true;
        for (int x = 0; x < 64; x ++) {
            for (int y = 0; y < 32; y ++) {
                if (gfx[x][y] == 1) {
                    valid = false;
                }
            }
        }
        // 'valid' should still be true if no pixel found
        assertEquals(valid, true);
    }

    @Test
    public void testSubReturn() { // 00EE
        // create an example sub call
        target.setStackAtIndex(0, 50);
        target.setStackPointer(1);
        // pc = opcode (0x2NNN sub call) & 0x0FFF
        target.setPC((0x2FE7 & 0x0FFF));

        // test the sub return
        target.OP00EE();
        // sp-- = 0
        assertEquals(target.getStackPointer(), 0);
        // pc = stack[sp] + 2 = 50 + 2
        assertEquals(target.getPC(), 52);
    }

    @Test
    public void testJump() { // 1NNN
        target.setOpcode(0x2659);

        target.OP1NNN();

        // pc = opcode & 0x0FFF = 0x659
        assertEquals(target.getPC(), 0x659);
    }

    @Test
    public void testCallSub() { // 2NNN
        target.setOpcode(0x2ABC);
        target.setPC(0x509);

        target.OP2NNN();

        // stack[sp] = pc, stack[0] = 0x509
        assertEquals(target.getStackAtIndex(target.getStackPointer()-1), 0x509);
        // sp++, sp = 1
        assertEquals(target.getStackPointer(), 1);
        // pc = opcode & 0x0FFF = 0xABC
        assertEquals(target.getPC(), 0xABC);
    }

    @Test
    public void testSkipIfEqualN() { // 0x3XNN
        target.setOpcode(0x3298);
        target.setVRegisterAtIndex(2, 152);

        target.OP3XKK();

        // V[2] = 152 = 0x98, pc = 0x200 + 4 = 516
        assertEquals(target.getPC(), 516);
    }

    @Test
    public void testSkipIfNotEqualN() { // 0x4XNN
        target.setOpcode(0x45DA);
        target.setVRegisterAtIndex(5, 43);

        target.OP4XKK();

        // V[5] = 43 != 0xDA, pc += 4 = 516
        assertEquals(target.getPC(), 516);
    }

    @Test
    public void testSkipIfEqualV() { // 5XY0
        target.setOpcode(0x5C80);
        target.setVRegisterAtIndex(0xC, 5);
        target.setVRegisterAtIndex(8, 5);

        target.OP5XY0();

        // V[C] = V[8] hence pc +4, = 516
        assertEquals(target.getPC(), 516);
    }

    @Test
    public void testSetAddress() { // 6XNN
        target.setOpcode(0x67ED);

        target.OP6XKK();

        // V[7] = 0xED = 237
        assertEquals(target.getVRegisterAtIndex(7), 237);
    }

    @Test
    public void testAdd() { // 7XNN
        target.setOpcode(0x7455);
        target.setVRegisterAtIndex(4, 39);

        target.OP7XKK();

        // V[4] = 39, NN = 0x55 = 85
        // 85 + 39 = 124
        assertEquals(target.getVRegisterAtIndex(4), 124);
    }

    @Test
    public void testSetV() { // 8XY0
        target.setOpcode(0x8750);
        target.setVRegisterAtIndex(0x7, 3);
        target.setVRegisterAtIndex(0x5, 9);

        target.OP8XY0();

        // V[7] = V[5] = 9
        assertEquals(target.getVRegisterAtIndex(7), 9);
    }

    @Test
    public void testOr() { // 8XY1
        target.setOpcode(0x84e1);
        target.setVRegisterAtIndex(4, 7);
        target.setVRegisterAtIndex(0xE, 42);

        target.OP8XY1();

        // 7 OR 42 = 47
        assertEquals(target.getVRegisterAtIndex(4), 47);
    }

    @Test
    public void testAnd() { // 8XY2
        target.setOpcode(0x8c02);
        target.setVRegisterAtIndex(0xC, 3);
        target.setVRegisterAtIndex(0, 5);

        target.OP8XY2();

        // 3 AND 5 = 1
        assertEquals(target.getVRegisterAtIndex(0xC), 1);
    }

    @Test
    public void testXor() { // 8XY3
        target.setOpcode(0x8483);
        target.setVRegisterAtIndex(4, 14);
        target.setVRegisterAtIndex(8, 31);

        target.OP8XY3();

        // 14 XOR 31 = 17
        assertEquals(target.getVRegisterAtIndex(0x4), 17);
    }

    @Test
    public void testAddCarry() { // 8XY4
        target.setOpcode(0x8394);
        target.setVRegisterAtIndex(3, 180);
        target.setVRegisterAtIndex(9, 155);

        target.OP8XY4();

        // 180 + 155 = 79
        assertEquals(target.getVRegisterAtIndex(3), 79);
        // overflow therefore V[F] = 1
        assertEquals(target.getVRegisterAtIndex(0xF), 1);
    }

    @Test
    public void testSubBorrow() { // 8XY5
        target.setOpcode(0x8015);
        target.setVRegisterAtIndex(0, 0);
        target.setVRegisterAtIndex(1, 2);

        target.OP8XY5();

        // V[0] -= V[1] = -2 = -254
        assertEquals(target.getVRegisterAtIndex(0), 254);
    }

    @Test
    public void testShiftRight() { // 8XY6
        target.setOpcode(0x8A56);
        target.setVRegisterAtIndex(0xA, 187);

        target.OP8XY6();

        // V[F] = LSB V[A] = 1
        assertEquals(target.getVRegisterAtIndex(0xF), 1);
        // V[A] = 187 >> 1 = 93
        assertEquals(target.getVRegisterAtIndex(0xA), 93);
    }

    @Test
    public void testSetSubBorrow() { // 8XY7
        target.setOpcode(0x8357);
        target.setVRegisterAtIndex(3, 124);
        target.setVRegisterAtIndex(5, 40);

        target.OP8XY7();

        // 40 - 124 = -84 = 172 underflow, V[F] = 0
        assertEquals(target.getVRegisterAtIndex(3), 172);
        assertEquals(target.getVRegisterAtIndex(0xF), 0);
    }

    @Test
    public void testShiftLeft() { // 8XYE
        target.setOpcode(0x89EE);
        target.setVRegisterAtIndex(9, 212);

        target.OP8XYE();

        // V[F] = MSB V[9] = 128
        assertEquals(target.getVRegisterAtIndex(0xF), 128);
        // V[9] = 212 << 1 = 424
        assertEquals(target.getVRegisterAtIndex(9), 424);
    }

    @Test
    public void testSkip() { // 9XY0
        target.setOpcode(0x9340);
        target.setVRegisterAtIndex(3, 50);
        target.setVRegisterAtIndex(4, 100);

        target.OP9XY0();
        // V[3] != V[4], pc += 4 = 516
        assertEquals(target.getPC(), 516);
    }

    @Test
    public void testSetIndex() { // ANNN
        target.setOpcode(0xA932);

        target.OPANNN();

        // index = 0x932 = 2354
        assertEquals(target.getIndexRegister(), 2354);
    }

    @Test
    public void testJumpV() { // BNNN
        target.setOpcode(0xB932);
        target.setVRegisterAtIndex(0, 356);

        target.OPBNNN();

        // pc = opcode + V[0] = 0x932 + 356 = 2710
        assertEquals(target.getPC(), 2710);
    }

    @Test
    public void testDrawSprite() { // DXYN
        target.setOpcode(0xDAE1);
        target.setVRegisterAtIndex(0xA, 3);
        target.setVRegisterAtIndex(0xE, 4);

        // create sprite
        int index = target.getIndexRegister();
        target.setMemoryAtIndex(index, 0x3C);

        target.OPDXYN();

        int[][] gfx = target.getGFX();
        // 0x3C = 00111100, start at 3,4 with 1 height

//        for (int i = 12; i <= 40; i++) {
//            System.out.println(gfx[i]);
//        }

        assertEquals(gfx[3][4], 0);
        assertEquals(gfx[4][4], 0);
        assertEquals(gfx[5][4], 1);
        assertEquals(gfx[6][4], 1);
        assertEquals(gfx[7][4], 1);
        assertEquals(gfx[8][4], 1);
        assertEquals(gfx[9][4], 0);
        assertEquals(gfx[10][4], 0);

    }

    @Test
    public void testSkipKeyPressed() { // EX9E
        target.setOpcode(0xE49E);
        target.setVRegisterAtIndex(4, 5);
        // simulate key press
        target.setKeyAtIndex(5, 1);

        target.OPEX9E();
        // key pressed, pc += 4 = 516
        assertEquals(target.getPC(), 516);
    }

    @Test
    public void testSkipKeyNotPressed() { // EXA1
        target.setOpcode(0xE49E);
        target.setVRegisterAtIndex(4, 5);
        target.setKeyAtIndex(5, 1);

        target.OPEXA1();
        // key not pressed, pc += 2 = 514
        assertEquals(target.getPC(), 514);
    }

    @Test
    public void testSetXDelay() { // FX07
        target.setOpcode(0xF407);
        target.setDelayTimer(35);

        target.OPFX07();

        // V[4] = delay = 35
        assertEquals(target.getVRegisterAtIndex(4), 35);
    }

    @Test
    public void testKeyWait() { // FX0A
        target.setOpcode(0xF30A);
        target.setKeyAtIndex(9, 1);

        target.OPFX0A();

        // V[3] = 9, pc +=2 = 514
        assertEquals(target.getVRegisterAtIndex(3), 9);
        assertEquals(target.getPC(), 514);
    }

    @Test
    public void testSetDelayX() { // FX15
        target.setOpcode(0xF915);
        target.setVRegisterAtIndex(9, 23);

        target.OPFX15();

        // delay = V[9] = 23
        assertEquals(target.getDelayTimer(), 23);
    }

    @Test
    public void testSetSoundX() { // FX18
        target.setOpcode(0xF318);
        target.setVRegisterAtIndex(3, 42);

        target.OPFX18();

        // sound = V[3] = 42
        assertEquals(target.getSoundTimer(), 42);
    }

    @Test
    public void testAddToIndex() { // FX1E
        target.setOpcode(0xF51E);
        target.setVRegisterAtIndex(5, 32);

        target.OPFX1E();

        // index += V[5] = 32
        assertEquals(target.getIndexRegister(), 32);
    }

    @Test
    public void testSetSpriteIndex() { // FX29
        target.setOpcode(0xF229);
        target.setVRegisterAtIndex(2, 4);

        target.OPFX29();

        // index = V[2] * 5 = 20
        assertEquals(target.getIndexRegister(), 20);
    }

    @Test
    public void testStoreBCD() { // FX33
        target.setOpcode(0xF633);
        target.setVRegisterAtIndex(6, 243);

        target.OPFX33();
        int index = target.getIndexRegister();
        // 243 = 2, 4, 3
        assertEquals(target.getMemoryAtIndex(index), 2);
        assertEquals(target.getMemoryAtIndex(index+1), 4);
        assertEquals(target.getMemoryAtIndex(index+2), 3);
    }

    @Test
    public void testMemStore() { // FX55
        target.setOpcode(0xF255);
        target.setVRegisterAtIndex(0, 43);
        target.setVRegisterAtIndex(1, 132);
        target.setVRegisterAtIndex(2, 14);

        target.OPFX55();
        int index = target.getIndexRegister();

        // M[i] = V[0], M[i+1] = V[2], M[i+2] = V[3]
        assertEquals(target.getMemoryAtIndex(index), 43);
        assertEquals(target.getMemoryAtIndex(index+1), 132);
        assertEquals(target.getMemoryAtIndex(index+2), 14);
    }

    @Test
    public void testMemFill() { // FX65
        target.setOpcode(0xF265);
        int index = target.getIndexRegister();
        target.setMemoryAtIndex(index, 213);
        target.setMemoryAtIndex(index+1, 112);
        target.setMemoryAtIndex(index+2, 453);

        target.OPFX65();

        // V[0] = M[i], V[1] = M[i+1], V[2] = M[i+2]
        // 453 -> 197 as V regs are 8bit
        assertEquals(target.getVRegisterAtIndex(0), 213);
        assertEquals(target.getVRegisterAtIndex(1), 112);
        assertEquals(target.getVRegisterAtIndex(2), 197);
    }
}
