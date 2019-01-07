/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chip8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author Jasoni7
 */
public class CPU {
        private static byte[] memory = new byte[0x1000];//4K of memory
        private static byte[] V = new byte[16];//All registers, array of V used for simplicity of reading
        private static short[] stack = new short[16];//stack, orginal spec. says 12, but most use 16.
        private static byte soundTimer = 0;//timer for sound *both timers decrements at 60Hz if not zero.
        private static byte delayTimer = 0;//timer for delay
        private static short PC = 0x200;//program counter
        private static byte SP = 0;//stack pointer
        private static short I = 0;//I "register", used for storing memory addresses in special instructions
        //font set for Chip 8
        final static int[] FONT = {0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
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
        
        public static void loadProgram(String file) throws FileNotFoundException, IOException {
            File binFile = new File(file);
            FileInputStream fileIn = new FileInputStream(binFile);
            byte[] fileData = new byte[(int)binFile.length()];
            fileIn.read(fileData);
            System.out.println("Length: " + fileData.length);
            for(int x = 0;x<fileData.length;x++){
                //System.out.println(Integer.toHexString(fileData[x]&0xFF));
                memory[x+0x200] = fileData[x];     
            }
            for(int x = 0; x < FONT.length;x++) {
                 memory[x] = (byte) (FONT[x]);
            }
        }

        public static void executeInstruction() {
            
                short instruction = (short) (memory[PC]<<8 | memory[PC+1] & 0xFF);//turn 2 bytes of memory into short for easier manipulation
                short nnn = (short)(instruction & 0x0FFF);//get 12 bit value, address of instruction
                byte x = (byte)(memory[PC] & 0xF);//get "X" register of an instruction,low 4 bits of high byte
                byte y = (byte)((memory[PC+1]>>4) & 0xF);//get "Y" register of an instruction, high 4 bits of low byte
                byte kk = (byte)(memory[PC+1]);//gets the low byte
                byte n = (byte)(memory[PC+1] & 0xF);//get the low 4 bits of low byte
                System.out.println("PC: " + Integer.toHexString(PC & 0xFFF) + ", Ins: " + (Integer.toHexString(instruction&0xFFFF)));
                System.out.println("V0: " + Integer.toHexString(V[0x0]&0xFF) + ", V1: " + Integer.toHexString(V[0x1]&0xFF) + ", V2: " + Integer.toHexString(V[0x2]&0xFF) + ",\n V3:" + 
                        Integer.toHexString(V[0x3]&0xFF) +", V4: " + Integer.toHexString(V[0x4]&0xFF) +", V5: " + Integer.toHexString(V[0x5]&0xFF) + ", V6: " + Integer.toHexString(V[0x6]&0xFF) + ", V7: " + Integer.toHexString(V[0x7]&0xFF) + ", V8: " + Integer.toHexString(V[0x8]&0xFF) + ",\n V9:" + 
                        Integer.toHexString(V[0x9]&0xFF) +", VA: " + Integer.toHexString(V[0xA]&0xFF) +", VB: " + Integer.toHexString(V[0xB]&0xFF) + ", VC: " + Integer.toHexString(V[0xC]&0xFF) + ", VD: " + Integer.toHexString(V[0xD]&0xFF) + ", V2: " + Integer.toHexString(V[0xE]&0xFF) + ",\n VE:" + 
                        Integer.toHexString(V[0xE]&0xFF) +", VF: " + Integer.toHexString(V[0xF]&0xFF)+ ", I:" + Integer.toHexString(I));
                System.out.println("N:" + Integer.toHexString(n&0xF) + ", X:" + Integer.toHexString(x&0xF) + ", Y:" + Integer.toHexString(y&0xF) + ",KK:" + Integer.toHexString(kk&0xFF));
                switch(((instruction & 0xF000) >> 12)&0xF) {
                    case 0x0:
                        {
                            if(instruction==0x00E0) { //Clears display -- CLS. 0nnn was a command used only by the original machines.
                                Chip8.clearDisplay();
                            }    
                            else {//Return from subroutine -- RET
                                SP--;
                                PC = stack[SP];
                            }
                        }break;
                    case 0x1: //Jump to Address -- JP addr
                        {
                            PC = nnn;
                            PC = (short) (PC - 2);
                        }break;
                    case 0x2: //Call subroutine -- CALL addr
                        {
                           
                            stack[SP] = PC;
                            SP++;
                            PC = nnn;
                            PC = (short) (PC - 2);
                        }break;
                    case 0x3: //Skip next instruction if Vx == kk -- SE Vx, byte
                        {
                            if(V[x] == kk) {
                                PC += 2;
                            }
                        }break;
                    case 0x4: //Skip next instruction if Vx != kk -- SNE Vx, byte
                        {
                            if(V[x] != kk) {
                                PC += 2;
                            }
                        }break;
                    case 0x5: //Skip next instruction if Vx = Vy -- SE Vx, Vy
                        {
                            if(V[x] == V[y]) {
                                PC += 2;
                            }
                        }break;
                    case 0x6: //Set Vx = kk -- LD Vx, byte
                        {
                            V[x] = kk;
                        }break;
                    case 0x7: //Set Vx += kk -- ADD Vx, byte
                        {
                            V[x] += kk;
                        }break;
                    case 0x8: 
                        {
                            switch(n&0xF) {
                                case 0x0: //Set Vx = Vy -- LD Vx, Vy
                                {
                                    V[x] = V[y];
                                }break;
                                case 0x1: //Set Vx = Vx OR Vy -- OR Vx, Vy
                                {
                                    V[x] = (byte)(V[x] | V[y]);
                                }break;
                                case 0x2: //Set Vx = Vx AND Vy -- AND Vx, Vy
                                {
                                    V[x] = (byte)(V[x] & V[y]);
                                }break;
                                case 0x3: //Set Vx = Vx XOR Vy -- XOR Vx, Vy
                                {
                                    V[x] = (byte)(V[x] ^ V[y]);
                                }break;
                                case 0x4: //Set Vx = Vx + Vy, set VF = Carry -- ADD Vx, Vy
                                {
                                    if((V[x] + V[y]) > 255) {
                                        V[0xF] = 0x01;
                                    }
                                    else {
                                        V[0xF] = 0x00;
                                    }
                                    
                                    V[x] = (byte)(V[x] + V[y]);
                                }break;
                                case 0x5: //Set Vx = Vx - Vy, set VF = not Borrow -- Vx = Vx - Vy
                                {
                                    if((V[x]&0xFF) > (V[y]&0xFF)) {
                                        V[0xF] = 0x1;
                                        V[x] = (byte)(V[x] - V[y]);
                                    }
                                    else {
                                        V[0xF] = 0x0;
                                        V[x] = (byte)(V[x] - V[y]);
                                    }
                                }break;
                                case 0x6: //Set Vx = Vx SHR 1 -- SHR Vx (Divide by 2)
                                {
                                    if((V[x] & 0x1) == 0x1) {
                                        V[0xF] = 1;
                                    }
                                    else {
                                        V[0xF] = 0;
                                    }
                                    int temp = ((V[x]>>1)&0xFF);
                                    V[x] = (byte)(temp);
                                }break;
                                case 0x7: //Set Vx = Vy - Vx, set VF = NOT borrow -- SUBN Vx, Vy
                                {
                                    if((V[y]&0xFF) > (V[x]&0xFF)) {
                                        V[0xF] = 0x1;
                                        V[x] = (byte)(V[y] - V[x]);
                                    }
                                    else {
                                        V[0xF] = 0x0;
                                        V[x] = (byte)(V[y] - V[x]);
                                    }
                                }break;
                                case 0xE: //Set Vx = Vx SHL 1 -- SHL Vx (Multiply by 2)
                                {
                                    if(((V[x]&0xFF) >> 7) == 0x1) {
                                        V[0xF] = 1;
                                    }
                                    else {
                                        V[0xF] = 0;
                                    }
                                    int temp = ((V[x]<<1)&0xFF);
                                    V[x] = (byte)(temp);
                                }break;
                                
                            }
                        }break;
                    case 0x9: //Skip next instruction if Vx != Vy -- SNE Vx,Vy
                        {
                            if(V[x] != V[y]) {
                                PC += 2;
                            }
                        }break;
                    case 0xA: //Set I = nnn -- LD I, addr
                        {
                            I = nnn;
                        }break;
                    case 0xB: //Jump to location nnn + V0 -- JP V0, addr
                        {
                            PC = (short)(nnn+V[0x0]);
                            PC = (short) (PC - 2);
                        }break;
                    case 0xC: //Set Vx = random byte AND kk -- RND Vx, byte
                        {
                            Random rand = new Random();
                            byte randNum = (byte)(rand.nextInt(256));
                            randNum = (byte)(randNum & kk);
                            V[x] = randNum;
                        }break;
                    case 0xD: //Display n-byte sprite starting at memory location I at (Vx,Vy), set VF = collision -- DRW Vx, Vy, nibble
                        {
                            boolean flag = false;
                            for (int xPixel = 0;xPixel<n;xPixel++) {
                                boolean[] pixels = decodeByte(memory[I+xPixel]);
                                for(int yPixel = 0;yPixel<8;yPixel++) {
                                    if(pixels[yPixel] == true) {
                                        boolean tempFlag = Chip8.drawPixel(V[x]+yPixel,V[y]+xPixel);
                                        if(tempFlag == true) {
                                            flag = true;
                                        }
                                    }
                                }
                            }
                            if (flag = true) {
                                V[0xF] = 1;
                            }
                            else {
                                V[0xF] = 0;
                            }
                        }break;
                    case 0xE: 
                        {
                        }break;
                    case 0xF: 
                        {
                            switch(kk&0xFF) {
                                case 0x07: 
                                    
                                {
                                }break;
                                case 0x0A: 
                                {
                                }break;
                                case 0x15: 
                                {
                                }break;
                                case 0x18: 
                                {
                                }break;
                                case 0x1E: //Set I = I + Vx -- ADD I, Vx
                                {
                                    I += V[x];
                                    V[0xF] = (byte) (I>>12);
                                }break;
                                case 0x29: //Set I = location of Digit Sprite for Vx -- LD F, Vx
                                {
                                   I = (short) (V[x] * 5);
                                }break;
                                case 0x33: //Store BCD of Vx on I to I+2 -- LD B, Vx
                                   int temp = (V[x]&0xFF);
                                   memory[I] = (byte)((temp%1000)/100);
                                   memory[I+1] = (byte)((temp%100)/10);
                                   memory[I+2] = (byte)(temp%10);
                                {
                                }break;
                                case 0x55: //Store V0 through Vx in location I, then increment I each time. -- LD [I], Vx
                                {
                                    for(int inc = 0;inc<=x;inc++) {
                                        memory[I+inc] = V[inc];
                                     
                                    }
                                    //I = (short) (I + x + 1);
                                }break;
                                case 0x65: //Load V0 through Vx at location I -- LD Vx, [I]
                                {
                                    for(int inc = 0;inc<=x;inc++) {
                                        V[inc] = memory[I+inc];
                                        
                                    }
                                    //I = (short) (I + x + 1);
                                }break;
                            }
                        }break;
                    default: {
                       System.out.println("BAD INSTRUCTION: " + instruction); 
                    }
                }
                PC += 2;
        }
        public static boolean[] decodeByte(byte b) {
            boolean[] returnBoolean = new boolean[8];
            b = (byte) (b & 0xFF);
           // System.out.println("Binary: " + Integer.toBinaryString(b&0xFF));
            for(int x = 7;x>=0;x--) {
                //System.out.println("Binary: " + Integer.toBinaryString(x&0xF));
                if((b&0x1) > 0) {
                    returnBoolean[x] = true;
                }
                else {
                    returnBoolean[x] = false;
                }
                b = (byte) (b >> 1);
            }
            return returnBoolean;
        }
}
