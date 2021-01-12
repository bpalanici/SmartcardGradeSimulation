/**
 * Copyright (c) 1998, 2019, Oracle and/or its affiliates. All rights reserved.
 *
 */
/*
 * @(#)Wallet.java	1.11 06/01/03
 */

package com.oracle.jcclassic.samples.wallet;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;

public class Wallet extends Applet {

    /* constants declaration */

    // code of CLA byte in the command APDU header
    final static byte Wallet_CLA = (byte) 0x80;

    // codes of INS byte in the command APDU header

    public final static byte VERIFY_PIN = (byte) 0x20;
    public final static byte UPDATE_PIN = (byte) 0x70;
    public final static byte UPDATE_ID = (byte) 0x71;
    public final static byte ADD_GRADE = (byte) 0x90;
    public final static byte GET_SPECIFIC_GRADES = (byte) 0x91;
    public final static byte GET_ALL_GRADES_ONE_SUBJECT = (byte) 0x92;
    public final static byte UPDATE_LAST_GRADE_ONE_SUBJECT = (byte) 0x93;
    public final static byte GET_ID = (byte) 0x98;


    // maximum number of incorrect tries before the PIN is blocked
    final static byte PIN_TRY_LIMIT = (byte) 0x03;

    // maximum size PIN
    final static byte MAX_PIN_SIZE = (byte) 0x08;

    // signal that the PIN verification failed
    final static short SW_VERIFICATION_FAILED = 0x5000;

    // signal the the PIN validation is required
    // for a credit or a debit transaction
    final static short SW_PIN_VERIFICATION_REQUIRED = 0x5001;

    // check if student it is registered to card
    final static short SW_ID_SET_REQUIRED = 0x5002;

    //check if a grade id is valid(0 -> 4)
    final static short SW_GRADE_ID_INVALID = 0x5003;

    //check if Le is big enough for request
    final static short SW_WRONG_RETURN_NR = 0x5004;

    final static byte NR_SUBJECTS = 5;


    /* instance variables declaration */
    OwnerPIN pin;
    int studentID;

    Subject[] subjects; // structure that represents the courses
    private Wallet(byte[] bArray, short bOffset, byte bLength) {

        // It is good programming practice to allocate
        // all the memory that an applet needs during
        // its lifetime inside the constructor
        pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);

        byte iLen = bArray[bOffset]; // aid length
        bOffset = (short) (bOffset + iLen + 1);
        byte cLen = bArray[bOffset]; // info length
        bOffset = (short) (bOffset + cLen + 1);
        byte aLen = bArray[bOffset]; // applet data length

        subjects = new Subject[NR_SUBJECTS + 1];
        for (byte i = 1 ; i <= NR_SUBJECTS; i++)
            subjects[i] = new Subject((byte) (i + 1));
        studentID = 0;
        // The installation parameters contain the PIN
        // initialization value
        pin.update(bArray, (short) (bOffset + 1), aLen);
        register();
    } // end of the constructor

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        // create a Wallet applet instance
        new Wallet(bArray, bOffset, bLength);
    } // end of install method

    @Override
    public boolean select() {
        // The applet declines to be selected
        // if the pin is blocked.
        return pin.getTriesRemaining() != 0;
    }// end of select method

    @Override
    public void deselect() {
        // reset the pin value
        pin.reset();
    }

    @Override
    public void process(APDU apdu) {

        // APDU object carries a byte array (buffer) to
        // transfer incoming and outgoing APDU header
        // and data bytes between card and CAD

        // At this point, only the first header bytes
        // [CLA, INS, P1, P2, P3] are available in
        // the APDU buffer.
        // The interface javacard.framework.ISO7816
        // declares constants to denote the offset of
        // these bytes in the APDU buffer

        byte[] buffer = apdu.getBuffer();
        // check SELECT APDU command

        if (apdu.isISOInterindustryCLA()) {
            if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
                return;
            }
            ISOException.throwIt((short)(ISO7816.SW_CLA_NOT_SUPPORTED + 1));
        }

        // verify the reset of commands have the
        // correct CLA byte, which specifies the
        // command structure
        if (buffer[ISO7816.OFFSET_CLA] != Wallet_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case VERIFY_PIN:
                verify(apdu);
                return;
            case UPDATE_PIN:
                updatePin(apdu);
                return;
            case UPDATE_ID:
                updateID(apdu);
                return;
            case ADD_GRADE:
                addGrade(apdu);
                return;
            case GET_SPECIFIC_GRADES:
                getSpecificGrades(apdu);
                return;
            case GET_ALL_GRADES_ONE_SUBJECT:
                getAllGradesOneSubject(apdu);
                return;
            case UPDATE_LAST_GRADE_ONE_SUBJECT:
                updateLastGradeOneSubject(apdu);
                return;
            case GET_ID:
                getID(apdu);
                return;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }

    } // end of process method

    private void verify(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        if (!pin.check(buffer, ISO7816.OFFSET_CDATA, numBytes)) {
            ISOException.throwIt(SW_VERIFICATION_FAILED);
        }

    } // end of validate method

    private void updatePin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        if (pin.getTriesRemaining() == 0) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        byte currentOffset = ISO7816.OFFSET_CDATA;
        byte firstPinLen = buffer[currentOffset];
        currentOffset++;
        if (!pin.check(buffer, currentOffset, firstPinLen))
            ISOException.throwIt((short)(SW_VERIFICATION_FAILED + 1));

        currentOffset += firstPinLen;
        byte secondPinLen = buffer[currentOffset];
        currentOffset++;
        pin.update(buffer, currentOffset, secondPinLen);
        pin.resetAndUnblock();

    } // end of update Pin method

    private void updateID(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        if (numBytes != 4) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        byte currentOffset = ISO7816.OFFSET_CDATA;
        studentID = 0;
        studentID += buffer[currentOffset + 3] & 0xFF;
        studentID += (buffer[currentOffset + 2] & 0xFF) << 8;
        studentID += (buffer[currentOffset + 1] & 0xFF) << 16;
        studentID += (buffer[currentOffset] & 0xFF) << 24;
    } // end of update ID method

    private void addGrade(APDU apdu) {
        // access authentication
        if (!pin.isValidated())
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        if (numBytes != byteRead || numBytes != 6)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte currentOffset = ISO7816.OFFSET_CDATA;
        byte gradeid = (byte)(buffer[currentOffset] & 0xFF); //number from 1 to 5
        if (gradeid > NR_SUBJECTS || gradeid <= 0)
            ISOException.throwIt(SW_GRADE_ID_INVALID);
        byte value = (byte)(buffer[currentOffset + 1] & 0xFF);
        byte day = (byte)(buffer[currentOffset + 2] & 0xFF);
        byte month = (byte)(buffer[currentOffset + 3] & 0xFF);
        short year = (short)(((buffer[currentOffset + 5] & 0xFF) << 8) + (buffer[currentOffset + 4] & 0xFF));
        subjects[gradeid].grades[subjects[gradeid].nrGrades] = new Grade(value, day, month, year);
        subjects[gradeid].nrGrades++; //incrementing the grade nr
        apdu.setOutgoing();
        buffer[0] = (byte) ((studentID >> 24) & 0xFF);
        buffer[1] = (byte) ((studentID >> 16) & 0xFF);
        buffer[2] = (byte) ((studentID >> 8) & 0xFF);
        buffer[3] = (byte) (studentID & 0xFF);
        apdu.setOutgoingLength((byte)4);
        apdu.sendBytes((short)0, (short)4);
    }

    private void getSpecificGrades(APDU apdu) {
        // access authentication
        if (!pin.isValidated())
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        apdu.getIncomingLength();
        if (numBytes != byteRead || numBytes > NR_SUBJECTS || numBytes <= 0)
            ISOException.throwIt((short)(ISO7816.SW_WRONG_LENGTH + 1));
        //getting the grades for each id
        for (byte i = 1; i <= numBytes; i++)  {
            byte gradeId = buffer[(byte)(ISO7816.OFFSET_CDATA + i - 1)];
            byte finalGrade = 0;
            for (byte i1 = 0; i1 < subjects[gradeId].nrGrades; i1++)
                if (subjects[gradeId].grades[i1].value <= 10) //we ignore erors -> 11
                    finalGrade = subjects[gradeId].grades[i1].value;
            buffer[i - 1] = finalGrade;
        }
        short le = apdu.setOutgoing();
        if (le < numBytes)
            ISOException.throwIt(SW_WRONG_RETURN_NR);
        apdu.setOutgoingLength(numBytes);
        apdu.sendBytes((short)0, numBytes);
    } // end of getSpecificGrades method


    private void getAllGradesOneSubject(APDU apdu) {
        // access authentication
        if (!pin.isValidated())
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        apdu.getIncomingLength();
        byte subjectID = buffer[ISO7816.OFFSET_CDATA];
        short nrItems = 0;
        for (byte i1 = 0; i1 < subjects[subjectID].nrGrades; i1++) {
            buffer[nrItems] = subjects[subjectID].grades[i1].value;
            nrItems++;
        }
        short le = apdu.setOutgoing();
        if (le < nrItems)
            ISOException.throwIt(SW_WRONG_RETURN_NR);
        apdu.setOutgoingLength(nrItems);
        apdu.sendBytes((short)0, nrItems);
    } // end of getAllGrades method

    private void updateLastGradeOneSubject(APDU apdu) {
        // access authentication
        if (!pin.isValidated())
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        if (numBytes != byteRead || numBytes != 2)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        byte currentOffset = ISO7816.OFFSET_CDATA;
        byte gradeid = (byte)(buffer[currentOffset] & 0xFF); //number from 1 to 5
        if (gradeid > NR_SUBJECTS || gradeid <= 0)
            ISOException.throwIt(SW_GRADE_ID_INVALID);
        subjects[gradeid].grades[subjects[gradeid].nrGrades - 1].value = (byte)(buffer[currentOffset + 1] & 0xFF);
    }

    private void getID(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        apdu.setOutgoing();
        buffer[0] = (byte) ((studentID >> 24) & 0xFF);
        buffer[1] = (byte) ((studentID >> 16) & 0xFF);
        buffer[2] = (byte) ((studentID >> 8) & 0xFF);
        buffer[3] = (byte) (studentID & 0xFF);
        apdu.setOutgoingLength((byte)4);
        apdu.sendBytes((short)0, (short)4);
    } // end of getID method


} // end of class Wallet

class Subject {
    byte id;
    Grade[] grades;
    byte nrGrades;
    Subject() {
        id = nrGrades = 0;
        grades = new Grade[10];
    }
    Subject(byte id) {
        this.id = id;
        nrGrades = 0;
        grades = new Grade[10];
    }
}

class Grade {
    byte value;
    byte day, month;
    short year;
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