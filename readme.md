# excel2mysql

    通过excel及db.properties即可进行简单建表/更新表的小工具
    
## 功能

+ 通过数据源及excel文件导入mysql database进行生成、更新表(支持create/update)
+ 支持xls xlsx
+ 支持创建表时指定存储引擎,默认InnoDB
+ 可跳过检查table schema及支持过滤table
+ 创建表支持PK(包含复合主键)、UK索引(单列)
+ 更新表支持PK(包含复合主键)、增加UK索引(单列)、新增/移除/变更列
+ 更新表存在配置索引进行处理时一般只增加不删除(除非PK变更,注:列移除会影响索引)    

## tips

 - 线上环境谨慎操作(后果自负)
 
 - 线上环境谨慎操作(后果自负)
 
 - 线上环境谨慎操作(后果自负)
  
 - 推荐使用环境: 本地环境(验证后再自行斟酌) 
 
 - 支持但不建议使用更新表提供的PK、UK能力


## 使用准备

### 数据源:  db.properties (参考)
    datasource.url: jdbc:mysql://localhost:3306/excel2mysql?serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false
    datasource.username: root
    datasource.password: 123456
    datasource.driver-class-name: com.mysql.cj.jdbc.Driver
      
### excel模板 (参考)

---
   + [/src/test/resources/create-excel.xls](src/test/resources/create-excel.xls)
---
   + [/src/test/resources/create-excel.xlsx](src/test/resources/create-excel.xlsx)
---
   + [/src/test/resources/update-excel.xlsx](src/test/resources/update-excel.xlsx)
---


### 命令

```
# 打包
mvn -DskipTests package

# 查看帮助
java -jar excel2mysql-1.0.0-SNAPSHOT.jar --help

# 导入xls进行创建表
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/create-excel.xls -excel-type xls

# 导入xls进行创建表 不检查table schema (即excel中可与数据源的数据库名称不一致)
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/create-excel.xls -excel-type xls -check-table-schema false

# 导入xls进行创建表 指定存储引擎为MyISAM 不检查table schema 
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -engine MyISAM -file-name ../src/test/resources/create-excel.xls -excel-type xls -check-table-schema false

# 导入xls进行创建表 (指定table - user, 即只导入user) 不检查table schema
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/create-excel.xls -excel-type xls -filter-table user -check-table-schema false

# 导入xls进行创建表 (指定多个table - user role, 即只导入user role) 不检查table schema
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/create-excel.xls -excel-type xls -filter-table "user role" -check-table-schema false

# 导入xls进行创建表 (排除table - user) 不检查table schema
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/create-excel.xls -excel-type xls -filter-table user -exclude-table -check-table-schema false

# 导入xlsx进行更新表 不检查table schema
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/update-excel.xlsx -excel-type xlsx -auto-mode update -check-table-schema false

```

### 命令参数

```
    --help, --h
    
    -auto-mode
      auto mode, values: create / update, default value create
    
    -check-table-schema
      check table schema, default value true
      Default: true
    
    * -data-source
      data source properties
    
    -excel-type
      excel type, values: xls / xlsx, default value xls
    
    -engine
      table engine, default value InnoDB

    -exclude-table
      exclude table mode
      Default: false
  
    -in-file-path
      in excel file path

    * -file-name
      in excel file name
    
    -filter-table
      filter table config, multiple values should be separated by space


```

### 其他

```

### COLUMN EXTRA ###

auto_increment  自增
on update CURRENT_TIMESTAMP 根据当前时间戳更新

### COLUMN KEY TYPE ###

PK 主键
UK 唯一键

### 更新excel表中的列映射关系 ###

remarks->(-)              表示列被移除
remarks->-                表示列被移除,支持但不推荐,使用(-)更容易区分

(-)->remark               表示列新增,即之前列不存在
-->remark                 表示列新增,支持但不推荐,使用(-)更容易区分

remarks->(remark)         表示列由当前的remarks更换为remark
(remarks)->(remark)       表示列由当前的remarks更换为remark
remarks->remark           表示列由当前的remarks更换为remark

remark                    表示列不变
```

