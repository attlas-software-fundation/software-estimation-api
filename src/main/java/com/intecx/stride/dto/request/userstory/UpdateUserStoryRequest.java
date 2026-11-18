package com.intecx.stride.dto.request.userstory;

import com.intecx.stride.enumeration.StoryStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStoryRequest extends CreateUserStoryRequest {

    private UUID id;

    private StoryStatus status;
}
