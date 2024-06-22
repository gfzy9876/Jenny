import { useEffect, useState } from "react";
import "./App.css";

function App() {
  const [imgPath, setImgPath] = useState("");
  useEffect(() => {
    let eventSource = new EventSource("http://192.168.1.3:40006/stream");
    eventSource.onmessage = (event) => {
      let data = JSON.parse(event.data);
      let imagePath = "../" + data.imagePath;
      console.log(imagePath);
      if (imagePath) {
        console.log("path = ", imagePath);
        setImgPath(imagePath);
      }
    };

    document.getElementById("selectImg")?.addEventListener("change", (e) => {
      let file = (e.target as HTMLInputElement).files?.[0];
      file && console.log(file.webkitRelativePath);

      // let formData = new FormData();
      // formData.append("image", file as File);
      // fetch("http://192.168.1.3:40006/upload", {
      //   method: "POST",
      //   body: formData,
      // });
    });
  }, []);
  return (
    <div className="App">
      <header className="App-header">
        <input id="selectImg" type="file" accept="image/*"></input>
        {imgPath && (
          <img src={imgPath} style={{ width: "20rem" }} alt="image"></img>
        )}
      </header>
    </div>
  );
}

export default App;
