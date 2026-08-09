# easyjava

`easyjava` 是一个基于 MySQL 元数据的 Java 代码生成器。它会读取数据库表结构，自动生成常见的分层代码，包括实体类、Mapper、Mapper XML、Query、Service、ServiceImpl、Controller，以及一组公共基础类。

## 项目特性

- 自动扫描数据库中的表
- 根据字段信息生成实体类
- 根据表结构生成常用分层代码
- 支持生成基础公共类
  - `DateTimePatternEnum`
  - `DateUtils`
  - `BaseMapper`
  - `BaseQuery`
  - `SimplePage`
  - `PaginationResultVO`
  - `ResponseCodeEnum`
  - `BusinessException`
  - `ResponseVO`
  - `BaseController`
  - `GlobalExceptionHandler`
- 支持常见生成规则
  - 表前缀去除
  - 字符串字段模糊查询
  - 日期时间字段区间查询
  - 日期格式化注解
  - `toString()` 自动生成

## 技术要求

- JDK 15
- Maven 3.x
- MySQL 5.7 或更高版本

## 目录结构

```text
src/main/java/com/easyjava
  RunApplication.java      # 程序入口
  bean/                    # 元数据对象和常量
  builder/                 # 代码生成逻辑
  utils/                   # 工具类
src/main/resources
  application.properties   # 生成器配置
  template/                # 基础代码模板
```

## 工作流程

1. 使用 JDBC 读取数据库表元数据。
2. 将表名、字段名转换为 Java 命名。
3. 先生成公共基础类。
4. 再按每张表生成业务代码。

## 配置说明

运行前请先修改 `src/main/resources/application.properties`。

### 数据库配置

```properties
db.driver.name=com.mysql.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/easylive?useUnicode=true&characterEncoding=utf8
db.username=root
db.password=你的密码
```

### 输出路径

```properties
path.base=C:\path\to\your\project\src\main
```

这里是生成代码的根目录，程序会把 Java 文件和资源文件输出到这个路径下。

### 包名配置

```properties
package.base=com.easylive
package.po=entity.po
package.vo=entity.vo
package.query=entity.query
package.utils=utils
package.enums=enums
package.mappers=mappers
package.service=service
package.service.impl=service.impl
package.exception=exception
package.controller=controller
```

### 生成规则

- `ignore.table.prefix=true`
  - 是否忽略表名前缀，默认会去掉第一个下划线前的内容。
- `suffix.bean.query=Query`
  - 查询对象后缀。
- `suffix.bean.query.fuzzy=Fuzzy`
  - 模糊查询字段后缀。
- `suffix.bean.query.time.start=Start`
  - 时间范围开始字段后缀。
- `suffix.bean.query.time.end=End`
  - 时间范围结束字段后缀。

## 生成结果

生成文件会写入 `path.base` 配置的目录中。

通常会生成以下内容：

- `src/main/java/.../entity/po`
- `src/main/java/.../entity/query`
- `src/main/java/.../mappers`
- `src/main/java/.../service`
- `src/main/java/.../service/impl`
- `src/main/java/.../controller`
- `src/main/resources/.../mappers/*.xml`

## 注意事项

- 程序会执行 `show table status` 读取所有表。
- 仅支持已识别的 SQL 类型。
- 会优先使用表注释和字段注释生成注释内容。
- 如果连接数据库失败，请检查 `db.url`、`db.username`、`db.password` 以及 MySQL 驱动版本。


