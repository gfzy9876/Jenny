# handle_error() {
#   python3 jenny.py
# }

# handle_pip_error() {
#   if ! pip show flask >/dev/null; then
#     echo "Flask is not installed. Installing Flask..."
#     pip install flask
#   else
#     echo "Flask is already installed."
#   fi

#   if ! pip show flask_cors >/dev/null; then
#     echo "Flask-Cors is not installed. Installing Flask-Cors..."
#     pip install flask_cors
#   else
#     echo "Flask-Cors is already installed."
#   fi
# }

# trap 'handle_pip_error' ERR
# if ! pip3 show flask >/dev/null; then
#   echo "Flask is not installed. Installing Flask..."
#   pip3 install flask
# else
#   echo "Flask is already installed."
# fi

# if ! pip3 show flask_cors >/dev/null; then
#   echo "Flask-Cors is not installed. Installing Flask-Cors..."
#   pip3 install flask_cors
# else
#   echo "Flask-Cors is already installed."
# fi

# trap 'handle_error' ERR
# python jenny.py



#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查Node.js
check_node() {
    if command -v node > /dev/null 2>&1; then
        NODE_VERSION=$(node --version)
        echo -e "${GREEN}✓ Node.js 已安装: $NODE_VERSION${NC}"
        return 0
    else
        echo -e "${RED}✗ Node.js 未安装${NC}"
        return 1
    fi
}

# 检查npm
check_npm() {
    if command -v npm > /dev/null 2>&1; then
        NPM_VERSION=$(npm --version)
        echo -e "${GREEN}✓ npm 已安装: v$NPM_VERSION${NC}"
        return 0
    else
        echo -e "${RED}✗ npm 未安装${NC}"
        return 1
    fi
}

# 检查目录
check_directory() {
    if [ -d "server" ]; then
        echo -e "${GREEN}✓ server 目录存在${NC}"
        return 0
    else
        echo -e "${RED}✗ server 目录不存在${NC}"
        return 1
    fi
}

# 安装Node.js使用Homebrew
install_with_brew() {
    echo -e "${BLUE}正在使用Homebrew安装Node.js...${NC}"
    
    # 检查是否已安装Homebrew
    if ! command -v brew > /dev/null 2>&1; then
        echo -e "${YELLOW}Homebrew未安装，正在安装Homebrew...${NC}"
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    fi
    
    # 安装Node.js
    brew install node
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Node.js 安装成功！${NC}"
        return 0
    else
        echo -e "${RED}Node.js 安装失败${NC}"
        return 1
    fi
}

# 显示安装选项
show_install_options() {
    echo -e "${YELLOW}请选择安装方式:${NC}"
    echo "1) 使用Homebrew安装 (推荐)"
    echo "2) 从官网下载安装"
    echo "3) 使用nvm安装"
    echo "4) 退出"
    echo -n "请输入选择 [1-4]: "
}

# 处理用户选择
handle_install_choice() {
    local choice
    read choice
    
    case $choice in
        1)
            install_with_brew
            ;;
        2)
            echo -e "${BLUE}请打开浏览器访问: https://nodejs.org/${NC}"
            echo "下载并安装Node.js后重新运行此脚本"
            exit 0
            ;;
        3)
            echo -e "${BLUE}正在安装nvm...${NC}"
            curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
            echo -e "${GREEN}nvm 安装完成，请重新打开终端后运行: nvm install --lts${NC}"
            exit 0
            ;;
        4)
            echo "退出安装"
            exit 0
            ;;
        *)
            echo -e "${RED}无效选择，请重新输入${NC}"
            show_install_options
            handle_install_choice
            ;;
    esac
}

# 询问用户是否要安装
ask_to_install() {
    echo -e "${YELLOW}是否要现在安装Node.js? (y/n)${NC}"
    read -p "请输入选择 [y/N]: " answer
    
    case $answer in
        [Yy]*)
            show_install_options
            handle_install_choice
            return 0
            ;;
        [Nn]*)
            echo "您选择了不安装，脚本退出"
            exit 0
            ;;
        *)
            echo "输入无效，默认选择不安装"
            exit 0
            ;;
    esac
}

# 主函数
main() {
    echo "检查运行环境..."
    echo "----------------------------------------"
    
    local node_ok=0
    local npm_ok=0
    local dir_ok=0
    
    # 检查环境
    if check_node; then
        node_ok=1
    fi
    
    if check_npm; then
        npm_ok=1
    fi
    
    if check_directory; then
        dir_ok=1
    fi
    
    echo "----------------------------------------"
    
    # 如果Node.js或npm未安装，询问用户
    if [ $node_ok -eq 0 ] || [ $npm_ok -eq 0 ]; then
        echo -e "${YELLOW}Node.js运行环境未安装或不全${NC}"
        ask_to_install
        
        # 安装后重新检查
        echo "重新检查环境..."
        if check_node && check_npm; then
            echo -e "${GREEN}环境检查通过！${NC}"
        else
            echo -e "${RED}安装后环境仍然不完整，请手动安装${NC}"
            exit 1
        fi
    fi
    
    # 检查目录
    if [ $dir_ok -eq 0 ]; then
        echo -e "${RED}server 目录不存在，请确保项目结构正确${NC}"
        exit 1
    fi
    
    # 所有检查通过，启动应用
    echo -e "${GREEN}环境检查通过，启动应用...${NC}"
    echo "----------------------------------------"
    cd server && npm install && node app.js
}

# 执行主函数
main
