package ru.evgeniy.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class AdsDTO {

    @Schema(description = "Общее количество объявлений")
    public int count;

    @JsonProperty(value = "results")
    public List<AdDTO> adDTOList;

    public AdsDTO(int count, List<AdDTO> adDTOList) {
        this.count = count;
        this.adDTOList = adDTOList;
    }
}