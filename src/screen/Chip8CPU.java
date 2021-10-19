package screen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;

public class Chip8CPU {
    private int opcode;
    private int[] memory = new int[4096];
    private int[] VRegister = new int[16];
    private int indexRegister;
    private int PC;
    private int[][] gfx = new int[64][32];
    private int soundTimer;
    private int delayTimer;
    private int[] stack = new int[16];
    private int stackPointer;
    private int[] key = new int[16];
    private boolean VF;
    private int[] fontSet = new int[] {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    public void initialize() {
        Arrays.fill(memory, 0);
        Arrays.fill(VRegister, 0);
        Arrays.fill(stack, 0);
        Arrays.fill(key, 0);
        opcode = 0;
        indexRegister = 0;
        PC = 512;
        soundTimer = 0;
        delayTimer = 0;
        stackPointer = 0;
        VF = true;

        for (int i = 0; i < 80; ++i) {
            memory[i] = fontSet[i];
        }
    }

    public void loadROM(String filePath) {
        Path path = Paths.get(filePath);
        try {
            byte[] data = Files.readAllBytes(path);
            for (int i = 0; i < data.length; i++) {
                memory[i + 512] = data[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayMemory() {
        for (int i = 0; i < 4096; i++) {
            System.out.print(memory[i] + " ");
            if (i%16==0 && i!=0) {
                System.out.println();
            }
        }
    }

    public void emulateCPUCycle() {
        opcode = fetchOpcode(PC);
        decode(opcode);
    }

    private int fetchOpcode(int PC) {
        return (memory[PC] & 0xFF) << 8 | (memory[PC + 1] & 0xFF);
    }

    public void decode(int opcode) {
        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode & 0x000F) {
                    case 0x0000:
                        OP00E0();
                        break;
                    case 0x000E:
                        OP00EE();
                        break;
                }
                break;
            case 0x1000:
                OP1NNN();
                break;
            case 0x2000:
                OP2NNN();
                break;
            case 0x3000:
                OP3XKK();
                break;
            case 0x4000:
                OP4XKK();
                break;
            case 0x5000:
                OP5XY0();
                break;
            case 0x6000:
                OP6XKK();
                break;
            case 0x7000:
                OP7XKK();
                break;
            case 0x8000:
                switch (opcode & 0xF00F) {
                    case 0x8000:
                        OP8XY0();
                        break;
                    case 0x8001:
                        OP8XY1();
                        break;
                    case 0x8002:
                        OP8XY2();
                        break;
                    case 0x8003:
                        OP8XY3();
                        break;
                    case 0x8004:
                        OP8XY4();
                        break;
                    case 0x8005:
                        OP8XY5();
                        break;
                    case 0x8006:
                        OP8XY6();
                        break;
                    case 0x8007:
                        OP8XY7();
                        break;
                    case 0x800E:
                        OP8XYE();
                        break;
                }
                break;
            case 0x9000:
                OP9XY0();
                break;
            case 0xA000:
                OPANNN();
                break;
            case 0xB000:
                OPBNNN();
                break;
            case 0xC000:
                OPCXKK();
                break;
            case 0xD000:
                OPDXYN();
                break;
            case 0xE000:
                switch (opcode & 0xF00F) {
                    case 0xE00E:
                        OPEX9E();
                        break;
                    case 0xE001:
                        OPEXA1();
                        break;
                }
                break;
            case 0xF000:
                switch (opcode & 0xF0FF) {
                    case 0xF007:
                        OPFX07();
                        break;
                    case 0xF00A:
                        OPFX0A();
                        break;
                    case 0xF015:
                        OPFX15();
                        break;
                    case 0xF018:
                        OPFX18();
                        break;
                    case 0xF01E:
                        OPFX1E();
                        break;
                    case 0xF029:
                        OPFX29();
                        break;
                    case 0xF033:
                        OPFX33();
                        break;
                    case 0xF055:
                        OPFX55();
                        break;
                    case 0xF065:
                        OPFX65();
                        break;
                }
                break;
            default:
                System.out.println("ERROR: Unknown opcode");
        }
    }

    public void OP00E0() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                gfx[x][y] = 0;
            }
        }
        PC += 2;
        VF = true;
    }

    public void OP00EE() {
        stackPointer--;
        PC = stack[stackPointer] + 2;
        VF = true;
    }

    public void OP1NNN() {
        PC = opcode & 0x0FFF;
    }

    public void OP2NNN() {
        stack[stackPointer] = PC;
        stackPointer++;
        PC = opcode & 0x0FFF;
    }

    public void OP3XKK() {
        if (VRegister[(opcode & 0x0F00) >> 8] == (opcode & 0x00FF)) {
            PC += 4;
        } else {
            PC += 2;
        }
    }

    public void OP4XKK() {
        if (VRegister[(opcode & 0x0F00) >> 8] != (opcode & 0x00FF)) {
            PC += 4;
        } else {
            PC += 2;
        }
    }

    public void OP5XY0() {
        if (VRegister[(opcode & 0x0F00) >> 8] == VRegister[(opcode & 0x00F0) >> 4]) {
            PC += 4;
        } else {
            PC += 2;
        }
    }

    public void OP6XKK() {
        VRegister[(opcode & 0x0F00) >> 8] = (opcode & 0x00FF);
        PC += 2;
    }

    public void OP7XKK() {
        int KK = (opcode & 0x00FF);
        int X = (opcode & 0x0F00) >> 8;
        int result = VRegister[X] + KK;
        if (result >= 256) {
            VRegister[X] = result - 256;
        } else {
            VRegister[X] = result;
        }
        PC += 2;
    }

    public void OP8XY0() {
        VRegister[(opcode & 0x0F00) >> 8] = VRegister[(opcode & 0x00F0) >> 4];
        PC += 2;
    }

    public void OP8XY1() {
        VRegister[(opcode & 0x0F00) >> 8] |= VRegister[(opcode & 0x00F0) >> 4];
        PC += 2;
    }

    public void OP8XY2() {
        VRegister[(opcode & 0x0F00) >> 8] &= VRegister[(opcode & 0x00F0) >> 4];
        PC += 2;
    }

    public void OP8XY3() {
        VRegister[(opcode & 0x0F00) >> 8] ^= VRegister[(opcode & 0x00F0) >> 4];
        PC += 2;
    }

    public void OP8XY4() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        int result = VRegister[X] + VRegister[Y];
        if (result > 255) {
            VRegister[15] = 1;
            VRegister[X] = (result - 256) & 0xFF;
        } else {
            VRegister[15] = 0;
            VRegister[X] = result & 0xFF;
        }
        PC += 2;
    }

    public void OP8XY5() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        if (VRegister[Y] > VRegister[X]) {
            VRegister[15] = 0;
            VRegister[X] = (256 + (VRegister[X] - VRegister[Y])) & 0xFF;
        } else {
            VRegister[15] = 1;
            VRegister[X] = (VRegister[X] - VRegister[Y]) & 0xFF;
        }
        PC += 2;
    }

    public void OP8XY6() {
        int X = (opcode & 0x0F00) >> 8;
        VRegister[15] = VRegister[X] & 0x01;
        VRegister[X] >>= 1;
        PC += 2;
    }

    public void OP8XY7() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        int result = VRegister[Y] - VRegister[X];
        if (VRegister[X] > VRegister[Y]) {
            VRegister[15] = 0;
            VRegister[X] = result + 256;
        } else {
            VRegister[15] = 1;
            VRegister[X] = result;
        }
        PC += 2;
    }

    public void OP8XYE() {
        int X = (opcode & 0x0F00) >> 8;
        VRegister[15] = VRegister[X] >> 7;
        VRegister[X] = VRegister[X] << 1;
        PC += 2;
    }

    public void OP9XY0() {
        if (VRegister[(opcode & 0x0F00) >> 8] != VRegister[(opcode & 0x00F0) >> 4]) {
            PC += 4;
        } else {
            PC += 2;
        }
    }

    public void OPANNN() {
        indexRegister = (opcode & 0x0FFF);
        PC += 2;
    }

    public void OPBNNN() {
        PC = (opcode & 0x0FFF) + VRegister[0];
    }

    public void OPCXKK() {
        Random random = new Random();
        int i = random.nextInt(256);
        VRegister[(opcode & 0x0F00) >> 8] = i & (opcode & 0x00FF);
        PC += 2;
    }

    public void OPDXYN() {
        int X = VRegister[(opcode & 0x0F00) >> 8];
        int Y = VRegister[(opcode & 0x00F0) >> 4];
        int spriteHeight = opcode & 0x000F;
        VRegister[15] = 0;
        for (int h = 0; h < spriteHeight; h++) {
            int pixel = memory[indexRegister + h];
            for (int w = 0; w < 8; w++) {
                if ((pixel & (0x80 >> w)) != 0) {
                    int xCoordinate = X + w;
                    int yCoordinate = Y + h;

                    if (xCoordinate < 64 && yCoordinate < 32) {
                        if (gfx[xCoordinate][yCoordinate] == 1) {
                            VRegister[15] = 1;
                        }
                        gfx[xCoordinate][yCoordinate] ^= 1;
                    }
                }
            }
        }
        VF = true;
        PC += 2;
    }

    public void OPEX9E() {
        if (key[VRegister[(opcode & 0x0F00) >> 8]] == 1) {
            PC += 4;
        } else {
            PC += 2;
        }
    }

    public void OPEXA1() {
        if (key[VRegister[(opcode & 0x0F00) >> 8]] == 0) {
            PC += 4;
        } else {
            PC += 2;
        }
    }

    public void OPFX07() {
        VRegister[(opcode & 0x0F00) >> 8] = delayTimer;
        PC += 2;
    }

    public void OPFX0A() {
        int X = (opcode & 0x0F00) >> 8;
        boolean keyPressed = false;

        for (int i = 0; i < 16; i++) {
            if (key[i] == 1) {
                VRegister[X] = i;
                keyPressed = true;
                key[i] = 0;
            }
        }

        if (keyPressed) {
            PC += 2;
        }
    }

    public void OPFX15() {
        delayTimer = VRegister[(opcode & 0x0F00) >> 8];
        PC += 2;
    }

    public void OPFX18() {
        soundTimer = VRegister[(opcode & 0x0F00) >> 8];
        PC += 2;
    }

    public void OPFX1E() {
        indexRegister += VRegister[(opcode & 0x0F00) >> 8];
        PC += 2;
    }

    public void OPFX29() {
        indexRegister = VRegister[(opcode & 0x0F00) >> 8] * 5;
        PC += 2;
        VF = true;
    }

    public void OPFX33() {
        int X = (opcode & 0x0F00) >> 8;
        memory[indexRegister] = VRegister[X] / 100;
        memory[indexRegister + 1] = (VRegister[X] % 100) / 10;
        memory[indexRegister + 2] = (VRegister[X] % 100) % 10;
        PC += 2;
    }

    public void OPFX55() {
        int X = (opcode & 0x0F00) >> 8;
        for (int i = 0; i <= X; i++) {
            memory[indexRegister + i] = VRegister[i];
        }
        PC += 2;
    }

    public void OPFX65() {
        int X = (opcode & 0x0F00) >> 8;
        for (int i = 0; i <= X; i++) {
            VRegister[i] = (memory[indexRegister + i]) & 0xFF;
        }
        PC += 2;
    }

    public void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }

        if (soundTimer > 0) {
            if (soundTimer == 1) {
                System.out.println("BEEP!");
            }
            soundTimer--;
        }
    }

    public int getOpcode() {
        return this.opcode;
    }

    public void setOpcode(int value) {
        this.opcode = value;
    }

    public int getMemoryAtIndex(int index) {
        return this.memory[index];
    }

    public void setMemoryAtIndex(int index, int value) {
        this.memory[index] = value;
    }

    public int getVRegisterAtIndex(int index) {
        return this.VRegister[index];
    }

    public void setVRegisterAtIndex(int index, int value) {
        this.VRegister[index] = value;
    }

    public int getIndexRegister() {
        return this.indexRegister;
    }

    public void setIndexRegister(int value) {
        this.indexRegister = value;
    }

    public int getPC() {
        return this.PC;
    }

    public void setPC(int value) {
        this.PC = value;
    }

    public int[][] getGFX() {
        return this.gfx;
    }

    public int getGFXAtXY(int x, int y) {
        return gfx[x][y];
    }

    public void setGFXAtXY(int x, int y) {
        this.gfx[x][y] ^= 1;
    }

    public int getSoundTimer() {
        return soundTimer;
    }

    public void setSoundTimer(int soundTimer) {
        this.soundTimer = soundTimer;
    }

    public int getDelayTimer() {
        return delayTimer;
    }

    public void setDelayTimer(int delayTimer) {
        this.delayTimer = delayTimer;
    }

    public int getStackAtIndex(int stackPointer) {
        return stack[stackPointer];
    }

    public void setStackAtIndex(int index, int value) {
        this.stack[index] = value;
    }

    public int getStackPointer() {
        return stackPointer;
    }

    public void setStackPointer(int stackPointer) {
        this.stackPointer = stackPointer;
    }

    public int[] getKey() {
        return key;
    }

    public void setKeyAtIndex(int index, int value) {
        this.key[index] = value;
    }

    public boolean isVF() {
        return VF;
    }

    public void setVF(boolean VF) {
        this.VF = VF;
    }

    public static void main(String[] args) {
        Chip8CPU c = new Chip8CPU();
        c.initialize();
        c.loadROM("../IBMLogo.ch8");
        c.displayMemory();
    }
}
