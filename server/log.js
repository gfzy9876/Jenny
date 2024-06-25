const Colors = {
  RED: "\x1b[31m",
  GREEN: "\x1b[32m",
  YELLOW: "\x1b[33m",
  RESET: "\x1b[0m",
};

const error = (msg) => console.log(`${Colors.RED}${msg}${Colors.RESET}`);
const warn = (msg) => console.log(`${Colors.YELLOW}${msg}${Colors.RESET}`);
const info = (msg) => console.log(`${Colors.RESET}${msg}${Colors.RESET}`);
const active = (msg) => console.log(`${Colors.GREEN}${msg}${Colors.RESET}`);

module.exports = {
  error,
  warn,
  info,
  active,
};
