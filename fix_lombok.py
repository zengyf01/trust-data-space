#!/usr/bin/env python3
"""
将 @Data 注解的实体类转换为手写 getter/setter 形式
(因为代码库里约定用 getfXxx / setfXxx 小写 f 风格)
"""
import re
import sys
import pathlib

def java_type_short(t: str) -> str:
    t = t.strip()
    return t.split('<')[0].split(' ')[-1].split('.')[-1]

def field_to_method(f: str) -> tuple:
    """fXxx -> (getfXxx, setfXxx)"""
    return f"get{f}", f"set{f}"

def fix_file(path: pathlib.Path):
    src = path.read_text(encoding="utf-8")
    if "@Data" not in src:
        return False

    # 1. 移除 lombok.Data import
    src = re.sub(r"^import lombok\.Data;\s*\n", "", src, flags=re.MULTILINE)
    # 2. 移除 @Data 注解
    src = re.sub(r"^@Data\s*\n", "", src, flags=re.MULTILINE)

    # 3. 收集字段
    field_pattern = re.compile(
        r"((?:@TableId|@TableField|@TableLogic|@Version|@IdType\.INPUT)[^\n]*\n)*"
        r"\s*private\s+([\w<>,\s\.]+?)\s+(\w+)\s*(?://[^\n]*)?;",
        re.MULTILINE,
    )
    fields = []
    for m in field_pattern.finditer(src):
        ann = (m.group(1) or "").strip()
        ftype = m.group(2).strip()
        fname = m.group(3).strip()
        fields.append((ann, ftype, fname))

    if not fields:
        print(f"WARN: no fields in {path}")
        return False

    # 4. 在类的最后一个 } 前插入 getter/setter
    getters_setters = "\n    // Getters and Setters\n"
    for ann, ftype, fname in fields:
        if ann:
            getters_setters += f"    {ann}\n"
        cap = java_type_short(ftype)
        getters_setters += f"    public {ftype} get{fname}() {{ return {fname}; }}\n"
        getters_setters += f"    public void set{fname}({ftype} {fname}) {{ this.{fname} = {fname}; }}\n"

    # 找到类的最后一个右大括号（匹配的）
    # 简单的：找最后一个 "}\n" 在类体内
    last_brace = src.rfind("}")
    if last_brace == -1:
        print(f"WARN: no closing brace in {path}")
        return False
    src = src[:last_brace] + getters_setters + src[last_brace:]

    path.write_text(src, encoding="utf-8")
    return True

if __name__ == "__main__":
    for p in sys.argv[1:]:
        path = pathlib.Path(p)
        if fix_file(path):
            print(f"Fixed: {path}")
