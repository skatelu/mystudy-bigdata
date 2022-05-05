# Yaml 语法

编程免不了要写配置文件，怎么写配置也是一门学问。YAML 是专门用来写配置文件的语言，非常简洁和强大，远比 JSON 格式方便

YAML 语言的设计目标，就是方便人类读写。它实质上是一种通用的数据串行化格式

## 基本语法规则

- 大小写敏感
- 使用缩进表示层级关系
- 缩进是不使用 tab 键，只允许使用空格
- 缩进的空格数目不重要，只要相同层级左侧的元素对其即可
- " # " 表示注释，从这个字符一直到行尾都会被解释器忽略

## 数据类型

### 对象类型：对象是一组键值对，使用冒号结构表示

```yaml
name: zhangsan

hash: {name: zhangsan, age: 18}
```



### 数组类型：一组连词线开头的行，构成一个数组

```yaml
- 
  - zhangsan
  - lisi
  - wangwu
  
{name: ['zhangsan', 'lisi', 'wangwu']}
```



### 复合结构：对象和数组可以结合使用，形成复合结构

```yaml
languages:
 - Ruby
 - Perl
 - Python 
websites:
 YAML: yaml.org 
 Ruby: ruby-lang.org 
 Python: python.org 
 Perl: use.perl.org 
```



### 纯量：纯量是最基本的、不可再分的值

- 字符串
- 布尔值
- 整数
- 浮点数
- Null
- 时间
- 日期

```yaml
# 数值直接以字面量的形式表示
number: 12.30

# 布尔值用 true 和 false 表示
isSet: true

# null 用 ~ 表示
parent: ~ 

# 时间采用 ISO8601 格式
iso8601: 2001-12-14t21:59:43.10-05:00

# 日期采用复合 iso8601 格式的年、月、日表示
date: 1976-07-31

# YAML 允许使用两个感叹号，强制转换数据类型
e: !!str 123
f: !!str true

# 字符串默认不使用引号表示
str: 这是一行字符串

# 如果字符串之中包含空格或特殊字符，需要放在引号之中
str: '内容： 字符串'

# 单引号和双引号都可以使用，双引号不会对特殊字符转义
s1: '内容\n字符串'
s2: "内容\n字符串"

# 单引号之中如果还有单引号，必须连续使用两个单引号转义
str: 'labor''s day' 

# 字符串可以写成多行，从第二行开始，必须有一个单空格缩进。换行符会被转为空格
str: 这是一段
  多行
  字符串
## { str: '这是一段 多行 字符串' }

# 多行字符串可以使用|保留换行符，也可以使用>折叠换行
this: |
  Foo
  Bar
that: >
  Foo
  Bar
## { this: 'Foo\nBar\n', that: 'Foo Bar\n' }

# +表示保留文字块末尾的换行，-表示删除字符串末尾的换行
s1: |
  Foo
 
s2: |+
  Foo
 
 
s3: |-
  Foo
  
## { s1: 'Foo\n', s2: 'Foo\n\n\n', s3: 'Foo' }

# 锚点&和别名*，可以用来引用
defaults: &defaults
  adapter:  postgres
  host:     localhost
 
development:
  database: myapp_development
  <<: *defaults
 
test:
  database: myapp_test
  <<: *defaults


```





