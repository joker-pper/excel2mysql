package com.joker17.excel2mysql.automode;

import com.joker17.excel2mysql.model.Excel2MysqlModel;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TableAutoModeSupportOptions {

    private String database;

    private String tableName;

    private String engine;

    private Boolean tableExists;

    private List<Excel2MysqlModel> tableExcel2MysqlModelList;
}
