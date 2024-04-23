# echo "请选择包名:"
# foods=("com.mabiliscash.debug" "com.loans.easypln.debug" "com.loan.fincash.debug" "Quit")
# select pkg in "${foods[@]}"; do
#     case $pkg in
#     "com.mabiliscash.debug")
#         echo "\033[32m菲律宾包名: $pkg\033[0m"
#         break
#         ;;
#     "com.loans.easypln.debug")
#         echo "\033[32m波兰包名: $pkg\033[0m"
#         break
#         ;;
#     "com.loan.fincash.debug")
#         echo "\033[32m泰国包名: $pkg\033[0m"
#         break
#         ;;
#     "Quit")
#         echo "\033[32mUser requested exit\033[0m"
#         exit
#         ;;
#     *) echo "invalid option $REPLY" ;;
#     esac
# done

handle_error() {
    python3 jenny.py ""
}

trap 'handle_error' ERR
python jenny.py ""
