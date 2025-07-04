package com.guilhermezuriel.reduceme.application.services.keygen.form;

public record CreateReducedUrlForm(
        String url,
        String description
) {
    public CreateReducedUrlForm() {
        this(null, null);
    }
}
