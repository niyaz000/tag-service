package com.tagservice.dto.error;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {
    private Long id;
    private OffsetDateTime deletedAt;
}
