#!/bin/bash

# Версия JavaFX
JAVAFX_VERSION="17.0.14"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"  # Папка, где находится скрипт
LINUX_SDK_ZIP="openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip"
WINDOWS_SDK_ZIP="openjfx-${JAVAFX_VERSION}_windows-x64_bin-sdk.zip"
LINUX_SDK_URL="https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/${LINUX_SDK_ZIP}"
WINDOWS_SDK_URL="https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/${WINDOWS_SDK_ZIP}"

# Пути к архивам (теперь они хранятся в папке со скриптом)
LINUX_SDK_ZIP_PATH="$SCRIPT_DIR/$LINUX_SDK_ZIP"
WINDOWS_SDK_ZIP_PATH="$SCRIPT_DIR/$WINDOWS_SDK_ZIP"

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

# Функция для загрузки и распаковки JavaFX SDK
download_and_extract() {
    local zip_file="$1"  # Путь к ZIP-архиву
    local url="$2"       # URL для загрузки
    local dest_dir="$3"  # Куда распаковывать

    if [[ -d "$dest_dir" ]]; then
        echo "✅ JavaFX SDK уже распакован в $dest_dir, пропускаем загрузку."
        return
    fi

    if [[ -f "$zip_file" ]]; then
        echo "📦 Найден локальный архив $zip_file, используем его для распаковки."
    else
        echo "📥 Скачивание JavaFX SDK с $url..."
        wget -q --show-progress -O "$zip_file" "$url" || { echo "❌ Ошибка при загрузке JavaFX SDK!"; exit 1; }
    fi

    echo "📦 Распаковка JavaFX SDK в $dest_dir..."
    unzip -q "$zip_file" -d "$TARGET_DIR/" || { echo "❌ Ошибка при распаковке JavaFX SDK!"; exit 1; }
    mv "$TARGET_DIR/javafx-sdk-${JAVAFX_VERSION}" "$dest_dir"
}

# Скачивание и распаковка JavaFX SDK
download_and_extract "$LINUX_SDK_ZIP_PATH" "$LINUX_SDK_URL" "$LINUX_SDK_DIR"
download_and_extract "$WINDOWS_SDK_ZIP_PATH" "$WINDOWS_SDK_URL" "$WINDOWS_SDK_DIR"

# Поиск JAR-файла в target/
JAR_FILE=$(find $TARGET_DIR -maxdepth 1 -name "*.jar" ! -name "original-*.jar" | head -n 1)
if [[ -z "$JAR_FILE" ]]; then
    echo "❌ JAR файл не найден в target/!"
    exit 1
fi
JAR_NAME=$(basename "$JAR_FILE")

# Копирование JAR-файла в обе папки
cp "$JAR_FILE" "$LINUX_DIR/"
cp "$JAR_FILE" "$WINDOWS_DIR/"

# Создание Linux запускающего скрипта (run.sh)
LINUX_SCRIPT="$LINUX_DIR/run.sh"
echo "🚀 Создание Linux-скрипта запуска: $LINUX_SCRIPT"
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
echo "🚀 Создание Windows-скрипта запуска: $WINDOWS_SCRIPT"
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

echo "✅ Готово! Ваше приложение можно запустить с помощью:"
echo "   Linux  -> ./target/LinuxX64/run.sh"
echo "   Windows -> ./target/WindowsX64/run.bat"
