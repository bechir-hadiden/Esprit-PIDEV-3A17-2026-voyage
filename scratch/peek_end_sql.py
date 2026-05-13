import json

log_path = r"C:\Users\med\.gemini\antigravity\brain\1ac58564-8d80-4cec-9abb-1acc34eda0e1\.system_generated\logs\overview.txt"

with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        if '"step_index":75' in line:
            try:
                data = json.loads(line)
                content = data.get('content', '')
                if content.startswith("<USER_REQUEST>"):
                    sql = content[len("<USER_REQUEST>"):].strip()
                    # The sql string is already truncated with "<truncated ...>"
                    print(sql[-2000:])
                break
            except:
                continue
