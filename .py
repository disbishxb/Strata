#!/usr/bin/env python3
import os

BASE = "/data/data/com.termux/files/home/build/Strata/docs/routing"

# zh-rCN
p = f"{BASE}/zh-rCN/80-text-editor.md"
s = open(p, encoding="utf-8").read()
anchor = "- **TextEditorScreen**：fork 增 STGX 语法选择、颜色选择、ProcessText 对话框、`textApps` 参数。"
add = "\n- **TextEditorScreen**：fork 增退出编辑器时自动收起输入法。"
if "退出编辑器时自动收起输入法" in s:
    print("SKIP zh: already present")
elif anchor in s:
    s = s.replace(anchor, anchor + add, 1)
    open(p, "w", encoding="utf-8").write(s)
    print("OK zh B.2")
else:
    print("NO ANCHOR zh")

# en
p = f"{BASE}/en/80-text-editor.md"
s = open(p, encoding="utf-8").read()
anchor = "- **TextEditorScreen**: the fork adds STGX grammar selection, color selection, ProcessText dialog, `textApps` param."
add = "\n- **TextEditorScreen**: the fork dismisses the keyboard automatically when leaving the editor."
if "dismisses the keyboard automatically" in s:
    print("SKIP en: already present")
elif anchor in s:
    s = s.replace(anchor, anchor + add, 1)
    open(p, "w", encoding="utf-8").write(s)
    print("OK en B.2")
else:
    print("NO ANCHOR en")