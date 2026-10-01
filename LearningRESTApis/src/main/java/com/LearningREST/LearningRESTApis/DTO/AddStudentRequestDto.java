package com.LearningREST.LearningRESTApis.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddStudentRequestDto {

    @NotBlank(message = "name should not be blank")
    @Size(min=2,max=30,message = "name should be of length 2 to 30 char")
    private String name;

    @Email
    @NotBlank(message = "email not null")
    private String email;
}
