package Structs;

public class Grade {
    public byte value;
    public byte day, month;
    public short year;
    Grade() {
        value = day = month = 0;
        year = 0;
    }
    public Grade(byte value, byte day, byte month, short year) {
        this.value = value;
        this.day = day;
        this.month = month;
        this.year = year;
    }
}