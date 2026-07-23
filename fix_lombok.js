// 将 @Data 注解的实体类转换为手写 getter/setter
// 命名风格: getfXxx / setfXxx (小写 f)
const fs = require('fs');
const path = require('path');

function javaTypeShort(t) {
  return t.split('<')[0].split(' ').pop().split('.').pop();
}

function fixFile(filePath) {
  let src = fs.readFileSync(filePath, 'utf-8');
  if (!src.includes('@Data')) return false;

  // 移除 lombok.Data import
  src = src.replace(/^import lombok\.Data;\s*\n/m, '');
  // 移除 @Data 注解
  src = src.replace(/^@Data\s*\n/m, '');

  // 收集字段
  const fieldRe = /((?:@(?:TableId|TableField|TableLogic|Version)|@IdType\.INPUT)[^\n]*\n)*[ \t]*private\s+([\w<>,\s\.]+?)\s+(\w+)\s*(?:\/\/[^\n]*)?;/gm;
  const fields = [];
  let m;
  while ((m = fieldRe.exec(src)) !== null) {
    const ann = (m[1] || '').trim();
    const ftype = m[2].trim();
    const fname = m[3].trim();
    fields.push({ ann, ftype, fname });
  }

  if (fields.length === 0) {
    console.log(`WARN: no fields in ${filePath}`);
    return false;
  }

  let gs = '\n    // Getters and Setters\n';
  for (const { ann, ftype, fname } of fields) {
    if (ann) gs += `    ${ann}\n`;
    gs += `    public ${ftype} get${fname}() { return ${fname}; }\n`;
    gs += `    public void set${fname}(${ftype} ${fname}) { this.${fname} = ${fname}; }\n`;
  }

  const lastBrace = src.lastIndexOf('}');
  if (lastBrace === -1) return false;
  src = src.slice(0, lastBrace) + gs + src.slice(lastBrace);

  fs.writeFileSync(filePath, src, 'utf-8');
  return true;
}

const files = process.argv.slice(2);
for (const f of files) {
  if (fixFile(f)) {
    console.log(`Fixed: ${f}`);
  }
}
