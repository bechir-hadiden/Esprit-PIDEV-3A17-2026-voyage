log_path = r"C:\Users\med\.gemini\antigravity\brain\1ac58564-8d80-4cec-9abb-1acc34eda0e1\.system_generated\logs\overview.txt"

with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        if 'INSERT INTO `voyages`' in line:
            pos = line.find('INSERT INTO `voyages`')
            print(line[pos:pos+500])
