handle_error() {
  python3 jenny.py
}

handle_pip_error() {
  if ! pip show flask >/dev/null; then
    echo "Flask is not installed. Installing Flask..."
    pip install flask
  else
    echo "Flask is already installed."
  fi
}

trap 'handle_pip_error' ERR
if ! pip3 show flask >/dev/null; then
  echo "Flask is not installed. Installing Flask..."
  pip3 install flask
else
  echo "Flask is already installed."
fi

trap 'handle_error' ERR
python jenny.py
