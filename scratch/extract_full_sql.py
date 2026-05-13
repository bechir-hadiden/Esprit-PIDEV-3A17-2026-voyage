import json

log_path = r"C:\Users\med\.gemini\antigravity\brain\1ac58564-8d80-4cec-9abb-1acc34eda0e1\.system_generated\logs\overview.txt"
output_path = r"c:\Users\med\Desktop\pidev\full_dump.sql"

with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        if '"step_index":75' in line:
            try:
                data = json.loads(line)
                content = data.get('content', '')
                if content.startswith("<USER_REQUEST>"):
                    sql = content[len("<USER_REQUEST>"):].strip()
                    with open(output_path, 'w', encoding='utf-8') as out:
                        out.write(sql)
                    print(f"Extracted {len(sql)} bytes to {output_path}")
                break
            except Exception as e:
                print(f"Error parsing line: {e}")
