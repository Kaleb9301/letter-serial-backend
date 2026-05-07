package com.bankofabyssinia.letter_serial_backend.dto.Response;

import com.bankofabyssinia.letter_serial_backend.entity.AuditLog;
import com.bankofabyssinia.letter_serial_backend.entity.Letter;
import com.bankofabyssinia.letter_serial_backend.entity.SerialNumber;
import java.util.List;

public class LetterWithDetails {
    private Letter letter;
    private SerialNumber serial;
    private List<AuditLog> auditLogs;

    public LetterWithDetails() {}

    // Getters and Setters
    public Letter getLetter() { return letter; }
    public void setLetter(Letter letter) { this.letter = letter; }

    public SerialNumber getSerial() { return serial; }
    public void setSerial(SerialNumber serial) { this.serial = serial; }

    public List<AuditLog> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(List<AuditLog> auditLogs) { this.auditLogs = auditLogs; }
}