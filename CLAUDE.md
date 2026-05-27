# CodeGraph 使用规则

在修改任何已有代码前，必须优先使用 CodeGraph MCP 工具理解代码结构。

工作流程：

1. 修改前，先使用 CodeGraph 查询相关 symbol、文件、调用链和影响范围。
2. 对 bug 修复、重构、接口变更、删除代码，必须先检查 callers / callees / impact。
3. 不要一开始就用 grep / 全局 Read 扫描项目；只有 CodeGraph 信息不足时才补充读取文件。
4. 修改前先简要说明：
   - 相关入口文件
   - 相关函数 / 类 / 方法
   - 可能受影响的调用方
   - 修改计划
5. 修改后运行必要测试、类型检查或构建命令。
6. 如果 CodeGraph 没有索引或状态异常，先提示我运行 `codegraph init -i` 或 `codegraph status`。