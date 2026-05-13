import json
import os

log_path = r"C:\Users\med\.gemini\antigravity\brain\1ac58564-8d80-4cec-9abb-1acc34eda0e1\.system_generated\logs\overview.txt"
output_path = r"c:\Users\med\Desktop\pidev\extracted_dump.sql"

found = False
with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        try:
            data = json.loads(line)
            if data.get('step_index') == 75:
                content = data.get('content', '')
                # The content starts with <USER_REQUEST>\n
                if content.startswith("<USER_REQUEST>"):
                    sql = content[len("<USER_REQUEST>"):].strip()
                    with open(output_path, 'w', encoding='utf-8') as out:
                        out.write(sql)
                    print(f"Successfully extracted SQL to {output_path}")
                    found = True
                    break
        except:
            continue

if not found:
    print("Could not find step 75 in the log file.")
