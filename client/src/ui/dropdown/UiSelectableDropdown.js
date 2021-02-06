import styled from "@emotion/styled";
import AppContext from "context/AppContext";
import PropTypes from "prop-types";
import React, {useContext} from "react";
import {Dropdown} from "react-bootstrap";
import {FaAngleDown} from "react-icons/all";
import tinycolor from "tinycolor2";
import {UiClassicButton} from "ui/button";
import {DropdownMenu, DropdownToggle} from "ui/dropdown/UiDropdown";

const SelectableDropdown = styled(UiClassicButton)`
  margin: 0;
  padding: 0;
  font-weight: bold;
  text-decoration: underline;
  color: var(--font-color);
  box-shadow: none;
  background-color: hsl(210, 17%, 98%);

  &:hover {
    box-shadow: none;
    text-decoration: underline;
    color: var(--font-color);
  }
  
  &:focus {
    background-color: hsl(210, 17%, 98%);;
  }
  
  .dark & {
    background-color: var(--dark-tertiary);
    padding: 0 .5rem;
    text-decoration: none;
    box-shadow: var(--dark-box-shadow) !important;

    &:hover {
      text-decoration: none;
      background-color: var(--dark-hover);
    }
  }
`;

const UiSelectableDropdown = (props) => {
    const {user, getTheme} = useContext(AppContext);
    const {id, className = null, currentValue, label, values} = props;
    let children;
    if (user.darkMode) {
        let color = getTheme().lighten(10);
        //if still not readable, increase again
        if (tinycolor.readability(color, "#282828") < 2.5) {
            color = color.lighten(25);
        }
        children = <SelectableDropdown label={label} as={DropdownToggle} id={id} variant={""} className={"move-top-1px"}
                                       style={{backgroundColor: getTheme().setAlpha(.1)}}>
            <span style={{color, marginRight: "0.2rem"}}>{currentValue}</span>
            <FaAngleDown style={{color: getTheme()}}/>
        </SelectableDropdown>
    } else {
        children = <SelectableDropdown label={label} as={DropdownToggle} id={id} variant={""} className={"move-top-1px"}>
            <span>{currentValue}</span>
            <FaAngleDown/>
        </SelectableDropdown>
    }
    return <Dropdown className={className} style={{zIndex: 1}}>
        {children}
        <DropdownMenu alignRight>{values}</DropdownMenu>
    </Dropdown>
};

UiSelectableDropdown.propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    currentValue: PropTypes.any.isRequired,
    values: PropTypes.array.isRequired
};

export {UiSelectableDropdown};