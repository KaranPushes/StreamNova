package com.streaming.authservice.util;

import org.springframework.stereotype.Component;

@Component
public class UserAgentParser {

    public String detectBrowser(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("PostmanRuntime")) {
            return "Postman";
        }

        if (userAgent.contains("Edg/")) {
            return "Edge";
        }

        if (userAgent.contains("Chrome/")) {
            return "Chrome";
        }

        if (userAgent.contains("Firefox/")) {
            return "Firefox";
        }

        if (userAgent.contains("Safari/")
                && !userAgent.contains("Chrome/")) {
            return "Safari";
        }

        return "Unknown";
    }

    public String detectOperatingSystem(String userAgent) {

        if (userAgent == null) {
            return "Unknown";
        }

        if (userAgent.contains("Windows")) {
            return "Windows";
        }

        if (userAgent.contains("Mac OS X")) {
            return "macOS";
        }

        if (userAgent.contains("Android")) {
            return "Android";
        }

        if (userAgent.contains("iPhone")) {
            return "iPhone";
        }

        if (userAgent.contains("Linux")) {
            return "Linux";
        }

        return "Unknown";
    }

    public String buildDeviceName(
            String browser,
            String operatingSystem) {

        if ("Unknown".equals(browser)
                && "Unknown".equals(operatingSystem)) {
            return "Unknown Device";
        }

        return browser + " on " + operatingSystem;
    }
}