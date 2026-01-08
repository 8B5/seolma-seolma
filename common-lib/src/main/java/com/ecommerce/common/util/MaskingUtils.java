package com.ecommerce.common.util;

import org.springframework.util.StringUtils;

public class MaskingUtils {
    
    private static final String MASK_CHAR = "*";
    
    /**
     * 이메일 마스킹 (예: test@example.com -> te**@example.com)
     */
    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];
        
        if (localPart.length() <= 2) {
            return email;
        }
        
        String maskedLocal = localPart.substring(0, 2) + 
                           MASK_CHAR.repeat(localPart.length() - 2);
        
        return maskedLocal + "@" + domainPart;
    }
    
    /**
     * 전화번호 마스킹 (예: 010-1234-5678 -> 010-****-5678)
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return phoneNumber;
        }
        
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
        
        if (cleanNumber.length() == 11) {
            return cleanNumber.substring(0, 3) + "-****-" + cleanNumber.substring(7);
        } else if (cleanNumber.length() == 10) {
            return cleanNumber.substring(0, 3) + "-***-" + cleanNumber.substring(6);
        }
        
        return phoneNumber;
    }
    
    /**
     * 이름 마스킹 (예: 홍길동 -> 홍*동)
     */
    public static String maskName(String name) {
        if (!StringUtils.hasText(name) || name.length() <= 1) {
            return name;
        }
        
        if (name.length() == 2) {
            return name.charAt(0) + MASK_CHAR;
        }
        
        return name.charAt(0) + MASK_CHAR.repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
}