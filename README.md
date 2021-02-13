# Debugging YAML Injection
Demo plugin, which injects the shell script language into each line of a YAML block scalar. 

```
./gradlew runIde
```

1. Create a new yaml file "test.yml"
2. Paste this content:
  ```yaml
  run: |-
    echo hello world
    echo hello world
  ```
3. Choose "Edit shell script fragment" from the context menu
4. The fragment is displayed incorrectly
5. Editing the fragment breaks the source