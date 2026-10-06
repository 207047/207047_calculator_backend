# 计算器后端

计算器的后端服务。负责检查表达式、计算结果，并把历史存进数据库。

## 技术栈

- Java 17
- Spring Boot 3
- Spring Data JPA
- H2 数据库

表达式是自己解析计算的，没有用 `eval`。

## 环境

- JDK 17+
- Maven 3.9+

## 安装和启动

```bash
mvn -DskipTests package
java -jar target/calculator-backend-1.0.0.jar
```

如果文件夹路径里有中文，建议用上面的 `java -jar`，不要用 `mvn spring-boot:run`。

启动后访问：http://localhost:8080

## 配置说明

配置在 `src/main/resources/application.yml`：

- 端口：8080
- 数据库：`jdbc:h2:file:./data/calculator`
- 用户名：`sa`
- 密码：空

## 数据库

用的是 H2 文件库。第一次启动时会自动建表 `calculation_history`，不用自己执行 SQL。

字段大概是：`id`、`expression`、`result`、`created_at`、`favorite`。

H2 控制台：http://localhost:8080/h2-console  
JDBC URL 填：`jdbc:h2:file:./data/calculator`

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/calculate` | 计算并保存 |
| GET | `/api/history` | 查历史，可用 page、size、keyword |
| DELETE | `/api/history/{id}` | 删一条 |
| DELETE | `/api/history` | 清空 |
| PUT | `/api/history/{id}/favorite` | 收藏 |
| GET | `/api/stats` | 统计 |
| POST | `/api/convert-base` | 进制转换 |

请求例子：

```json
{"expression":"(1+2)*3"}
```

成功：

```json
{"success":true,"expression":"(1+2)*3","result":9.0}
```

失败：

```json
{"success":false,"message":"Invalid expression"}
```

## 和前端怎么连

开发时前端通过 Vite 代理访问这个服务。跨域已经放开了 `/api/**`。

## 测试

```bash
mvn test
```

## 在线地址

http://125.208.17.18:8888/
