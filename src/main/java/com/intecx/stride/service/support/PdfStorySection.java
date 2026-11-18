package com.intecx.stride.service.support;

import java.util.List;

public record PdfStorySection(
        String code,
        String title,
        Integer points,
        List<PdfTaskLine> tasks
) {
}
