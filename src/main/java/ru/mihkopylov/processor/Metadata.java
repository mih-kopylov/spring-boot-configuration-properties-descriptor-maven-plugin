package ru.mihkopylov.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {
    @NonNull
    private List<Property> properties = new ArrayList<>();

    @Getter
    @Setter
    @ToString
    public static class Property {
        @NonNull
        private String name;
        @NonNull
        private String type;
        @Nullable
        private String description;
        @NonNull
        private String sourceType;
        @Nullable
        private String defaultValue;
    }
}
