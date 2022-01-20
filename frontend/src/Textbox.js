import './zTextbox.css';

function TextBox({
                   id, label, placeholder, extraClass = "", type,
                   value, onChangeFunc, extraLabelAttrs = {}, extraAttrs = {}
                 }) {

  return (
      <div className={"TextBox " + extraClass}>
        <label htmlFor={id} {...extraLabelAttrs}>{label}</label>
        <input id={id} className="TextBoxInput"
               type={type} step="any" placeholder={placeholder}
               required aria-required autoComplete="off"
               onChange={e => onChangeFunc(e.target.value)} value={value}
               {...extraAttrs}>
        </input>
      </div>
  )
}

export default TextBox;