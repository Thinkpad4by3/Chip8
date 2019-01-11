# Chip8
Chip8 emulator in Java - not fully compatible and missing instructions but in progress

# How it works
This Chip 8 emulator simulates the CPU and Graphics of a Chip 8 computer. Each binary file is loaded in to a 4K byte array of which the CPU class analyzes each instruction and figures out the appropriate action according to the Chip-8 standard. The instructions are broken down by certain bits in the "opcode" and then the "used" bits are put through the intended operation. The graphics are handled by a JavaFX Writable Image through Chip8's CPU-driven sprite-based graphics. The image is scaled up by an integer factor to make the 64x32 monochrome image viewable on modern high-res displays.

#Things to Do
Need to add the instructions dealing with sound, keyboard, and time processing. Also need to fix a few bugs with one or two of the instructions.

#References used
Cowgod's Chip 8 Technical Reference: http://devernay.free.fr/hacks/chip8/C8TECH10.HTM
