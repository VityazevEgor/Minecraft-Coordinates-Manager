#!/bin/bash

# Версия JavaFX
JAVAFX_VERSION="17.0.14"
LINUX_SDK_URL="https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip"
WINDOWS_SDK_URL="https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/openjfx-${JAVAFX_VERSION}_windows-x64_bin-sdk.zip"

# Папки для сборки
TARGET_DIR="target"
LINUX_DIR="$TARGET_DIR/LinuxX64"
WINDOWS_DIR="$TARGET_DIR/WindowsX64"
LINUX_SDK_DIR="$LINUX_DIR/javafx-sdk-${JAVAFX_VERSION}"
WINDOWS_SDK_DIR="$WINDOWS_DIR/javafx-sdk-${JAVAFX_VERSION}"

# Очистка и сборка проекта
echo "🔧 Cleaning and building project with Maven..."
mvn clean package || { echo "❌ Build failed!"; exit 1; }

# Удаление "original-*.jar" файлов из target/
echo "🗑 Removing original-*.jar files..."
rm -f $TARGET_DIR/original-*.jar

# Создание выходных папок
mkdir -p "$LINUX_DIR" "$WINDOWS_DIR"

# Функция для скачивания и распаковки JavaFX SDK
download_and_extract() {
    local url="$1"
    local dest_dir="$2"
    local zip_file="${dest_dir}.zip"

    echo "📥 Downloading JavaFX SDK from $url..."
    wget -q --show-progress -O "$zip_file" "$url" || { echo "❌ Failed to download JavaFX SDK!"; exit 1; }

    echo "📦 Extracting JavaFX SDK to $dest_dir..."
    unzip -q "$zip_file" -d "$TARGET_DIR/" || { echo "❌ Failed to extract JavaFX SDK!"; exit 1; }
    mv "$TARGET_DIR/javafx-sdk-${JAVAFX_VERSION}" "$dest_dir"
    rm "$zip_file"
}

# Скачивание и распаковка JavaFX SDK
download_and_extract "$LINUX_SDK_URL" "$LINUX_SDK_DIR"
download_and_extract "$WINDOWS_SDK_URL" "$WINDOWS_SDK_DIR"

# Поиск JAR-файла в target/
JAR_FILE=$(find $TARGET_DIR -maxdepth 1 -name "*.jar" ! -name "original-*.jar" | head -n 1)
if [[ -z "$JAR_FILE" ]]; then
    echo "❌ JAR file not found in target/!"
    exit 1
fi
JAR_NAME=$(basename "$JAR_FILE")

# Копирование JAR-файла в обе папки
cp "$JAR_FILE" "$LINUX_DIR/"
cp "$JAR_FILE" "$WINDOWS_DIR/"

# Создание Linux запускающего скрипта (run.sh)
LINUX_SCRIPT="$LINUX_DIR/run.sh"
echo "🚀 Creating Linux launch script: $LINUX_SCRIPT"
cat > "$LINUX_SCRIPT" <<EOF
#!/bin/bash
DIR="\$(cd "\$(dirname "\${BASH_SOURCE[0]}")" && pwd)"
java -p "\$DIR/javafx-sdk-${JAVAFX_VERSION}/lib" \\
    --add-modules javafx.controls,javafx.base,javafx.fxml,javafx.graphics,javafx.media,javafx.web \\
    --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED \\
    --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \\
    -jar "\$DIR/$JAR_NAME"
EOF
chmod +x "$LINUX_SCRIPT"

# Создание Windows запускающего скрипта (run.bat)
WINDOWS_SCRIPT="$WINDOWS_DIR/run.bat"
echo "🚀 Creating Windows launch script: $WINDOWS_SCRIPT"
cat > "$WINDOWS_SCRIPT" <<EOF
@echo off
setlocal enabledelayedexpansion

set DIR=%~dp0
for %%F in ("%DIR%\*.jar") do set JAR_NAME=%%F

if not defined JAR_NAME (
    echo Error: No JAR file found in %DIR%
    exit /b 1
)

java -p "%DIR%\javafx-sdk-${JAVAFX_VERSION}\lib" ^
    --add-modules javafx.controls,javafx.base,javafx.fxml,javafx.graphics,javafx.media,javafx.web ^
    --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED ^
    --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED ^
    -jar "%JAR_NAME%"
EOF


echo "✅ Done! You can run your app using:"
echo "   Linux  -> ./target/LinuxX64/run.sh"
echo "   Windows -> ./target/WindowsX64/run.bat"
