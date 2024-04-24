if ! pip show flask >/dev/null; then
  echo "Flask is not installed. Installing Flask..."
  pip install flask
else
  echo "Flask is already installed."
fi

handle_error() {
  python3 jenny.py ""
}

trap 'handle_error' ERR
python jenny.py ""
