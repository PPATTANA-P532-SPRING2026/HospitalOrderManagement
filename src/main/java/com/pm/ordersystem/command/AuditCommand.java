package com.pm.ordersystem.command;

public class AuditCommand implements Command {
    private final String detail;

    public AuditCommand(String detail) {
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public void execute() {
        // no-op
    }


}