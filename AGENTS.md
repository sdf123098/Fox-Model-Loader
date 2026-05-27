# CodeGraph 强制规则

## 总原则

在处理任何代码库相关任务时，必须优先使用 CodeGraph。

CodeGraph 是代码理解、搜索、依赖分析、调用关系分析和影响范围分析的首选工具。只有在 CodeGraph 结果不足、需要确认具体实现细节、或需要修改文件时，才允许直接读取源码文件。

## 启动后第一步

每次开始分析项目时，必须先检查 CodeGraph 状态。

优先调用：

- `codegraph_status`

如果发现索引缺失、索引过期、项目未初始化、或 CodeGraph 不可用，必须立即说明问题，并要求先更新 CodeGraph 索引。

不得在 CodeGraph 未就绪的情况下假装已经分析完整代码库。

## 禁止行为

在 CodeGraph 可用的情况下，禁止一开始就使用以下方式大范围扫描项目：

- `ls`
- `dir`
- `find`
- `grep`
- `rg`
- `Get-ChildItem`
- 大范围 `Read`
- 手动遍历目录
- 盲目打开大量源码文件

这些方式只能在 CodeGraph 已经给出定位结果后，用于补充确认具体文件内容。

## 必须优先使用的 CodeGraph 能力

### 查找代码、符号、模块

优先使用：

- `codegraph_search`
- `codegraph_context`
- `codegraph_explore`
- `codegraph_files`

### 理解函数、类、模块关系

优先使用：

- `codegraph_node`
- `codegraph_context`
- `codegraph_explore`

### 分析调用关系

优先使用：

- `codegraph_callers`
- `codegraph_callees`

### 分析修改影响范围

优先使用：

- `codegraph_impact`

### 分析 bug、重构、兼容性问题

必须先使用 CodeGraph 找到相关符号、调用链、依赖关系和影响范围，再读取具体文件。

## 工作流程

处理代码任务时，应遵守以下流程：

1. 先调用 `codegraph_status` 检查索引状态。
2. 使用 CodeGraph 定位相关文件、函数、类、接口或模块。
3. 使用 CodeGraph 分析调用关系、依赖关系和影响范围。
4. 只读取 CodeGraph 指向的必要文件。
5. 修改代码前，先说明 CodeGraph 分析得到的影响范围。
6. 修改后，结合 CodeGraph 结果检查是否还有相关调用点需要同步修改。

## 修改代码时的规则

修改代码前必须先确认：

- 要修改的符号在哪里定义。
- 哪些地方调用了它。
- 哪些模块依赖它。
- 修改后可能影响哪些路径。
- 是否存在同名、相似名或重载实现。

不得只凭单个文件内容直接修改跨模块逻辑。

## CodeGraph 不可用时

如果 CodeGraph MCP 不可用、CodeGraph 索引不存在、或 CodeGraph 查询失败，必须明确说明：

- CodeGraph 当前不可用。
- 无法保证已经完整理解项目结构。
- 需要先运行 CodeGraph 更新或初始化索引。

在这种情况下，允许绕过 CodeGraph

## 回答格式

回答代码库问题时，优先说明：

1. CodeGraph 状态是否正常。
2. 通过 CodeGraph 找到了哪些关键文件、符号或调用关系。
3. 修改或分析的影响范围。
4. 需要读取源码确认的具体文件。
5. 最终结论或修改方案。

## 绝对要求

只要任务涉及代码库理解、代码修改、bug 定位、重构、依赖分析、架构分析、接口分析、调用链分析，就必须优先使用 CodeGraph。