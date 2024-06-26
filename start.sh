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

cd server && npm install && node app.js
