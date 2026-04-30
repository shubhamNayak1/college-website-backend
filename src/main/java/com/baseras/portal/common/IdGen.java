package com.baseras.portal.common;

import java.security.SecureRandom;

public class IdGen {
    private static final SecureRandom RND = new SecureRandom();
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    public static String of(String prefix) {
        StringBuilder sb = new StringBuilder(prefix).append('_');
        for (int i = 0; i < 10; i++) sb.append(ALPHABET[RND.nextInt(ALPHABET.length)]);
        return sb.toString();
    }

    public static String defaultStudentPassword(String rollNumber, String firstName, String dob) {
        int yob;
        try { yob = Integer.parseInt(dob.substring(0, 4)); }
        catch (Exception e) { yob = 0; }
        String namePart = firstName == null ? "" : firstName.replaceAll("[^a-zA-Z]", "");
        if (namePart.length() > 3) namePart = namePart.substring(0, 3);
        if (!namePart.isEmpty()) {
            namePart = Character.toUpperCase(namePart.charAt(0)) + namePart.substring(1).toLowerCase();
        }
        return rollNumber + namePart + yob;
    }

    private IdGen() {}
}
