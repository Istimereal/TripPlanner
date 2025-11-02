package app.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PackingListDTO {

    List<ItemForPackagingDTO> items;

}
