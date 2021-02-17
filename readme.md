# excel2mysql

    通过excel及db.properties即可进行简单建表的小工具
    
## 功能

+ *通过数据源及excel文件导入mysql database进行建表(支持create)*
+ *支持 PK UK 索引*
+ *支持xls xlsx*
+ *可跳过检查table schema*
    

## 使用

### 数据源:  db.properties
    datasource.url: jdbc:mysql://localhost:3306/excel2mysql?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8&useSSL=false
    datasource.username: root
    datasource.password: 123456
    datasource.driver-class-name: com.mysql.cj.jdbc.Driver
      
### excel模板

---
   + [/src/test/resources/test.xls](https://github.com/joker-pper/excel2mysql/blob/master/src/test/resources/test.xls)
---

   + [/src/test/resources/test.xlsx](https://github.com/joker-pper/excel2mysql/blob/master/src/test/resources/test.xlsx)
---

## tips

 - 线上环境谨慎操作
 
 - 线上环境谨慎操作
 
 - 线上环境谨慎操作


## 命令

``` 

# 查看帮助
java -jar excel2mysql-1.0.0-SNAPSHOT.jar --help

# 导入xls
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/test -excel-type xls


# 导入xls不检查table schema (即excel中可与数据源的数据库名称不一致)
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/test -excel-type xls -check-table-schema false

# 导入xls (指定table - user) 不检查table schema
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/test -excel-type xls -filter-table user -check-table-schema false

# 导入xls (排除table - user) 不检查table schema
java -jar excel2mysql-1.0.0-SNAPSHOT.jar -data-source ../src/test/resources/db.properties -file-name ../src/test/resources/test -excel-type xls -filter-table user -exclude -check-table-schema false

``` 

