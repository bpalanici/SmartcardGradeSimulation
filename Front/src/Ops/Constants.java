package Ops;

public class Constants {
    /* constants declaration */

    // code of CLA byte in the command APDU header
    public final static byte Wallet_CLA = (byte) 0x80;

    // codes of INS byte in the command APDU header

    public final static byte VERIFY_PIN = (byte) 0x20;
    public final static byte UPDATE_PIN = (byte) 0x70;
    public final static byte UPDATE_ID = (byte) 0x71;
    public final static byte ADD_GRADE = (byte) 0x90;
    public final static byte GET_SPECIFIC_GRADES = (byte) 0x91;
    public final static byte GET_ALL_GRADES_ONE_SUBJECT = (byte) 0x92;
    public final static byte GET_ID = (byte) 0x98;

    public static byte NR_SUBJECTS = 5;

    // maximum number of incorrect tries before the PIN is blocked
    public final static byte PIN_TRY_LIMIT = (byte) 0x03;

    // maximum size PIN
    public final static byte MAX_PIN_SIZE = (byte) 0x08;

    // signal that the PIN verification failed
    public final static short SW_VERIFICATION_FAILED = 0x5000;

    // signal the the PIN validation is required
    // for a credit or a debit transaction
    public final static short SW_PIN_VERIFICATION_REQUIRED = 0x5001;

    // check if student it is registered to card
    public final static short SW_ID_SET_REQUIRED = 0x5002;

    //check if a grade id is valid(0 -> 4)
    public final static short SW_GRADE_ID_INVALID = 0x5003;

    //check if Le is big enough for request
    public final static short SW_WRONG_RETURN_NR = 0x5004;

    public final static int STUDENT_TEST_ID = 0x102;
}
