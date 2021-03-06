import axios from "axios";
import {AppContext, BoardContext} from "context";
import React, {useContext, useState} from 'react';
import TextareaAutosize from "react-autosize-textarea";
import {UiClickableTip} from "ui";
import {UiLoadableButton} from "ui/button";
import {UiFormControl, UiFormLabel, UiMarkdownFormControl} from "ui/form";
import {UiCol} from "ui/grid";
import {UiDismissibleModal} from "ui/modal";
import {formatRemainingCharacters, popupError, popupNotification, popupWarning} from "utils/basic-utils";

const ChangelogUpdateModal = ({isOpen, onHide, changelog, onChangelogUpdate}) => {
    const {getTheme} = useContext(AppContext);
    const {discriminator} = useContext(BoardContext).data;
    const [title, setTitle] = useState(changelog.title);

    const handleSubmit = () => {
        const description = document.getElementById("descriptionTextarea").value;
        if (title.length < 10) {
            popupWarning("Title should be at least 10 characters long");
            return Promise.resolve();
        }
        if (description.length < 20) {
            popupWarning("Description should be at least 20 characters long");
            return Promise.resolve();
        }
        return axios.patch("/boards/" + discriminator + "/changelog/", {
            title, description
        }).then(res => {
            if (res.status !== 200 && res.status !== 201) {
                popupError();
                return;
            }
            popupNotification("Changelog updated", getTheme());
            setTitle("");
            onHide();
            onChangelogUpdate(res.data);
        });
    };

    return <UiDismissibleModal id={"changelogPost"} isOpen={isOpen} onHide={onHide} title={"Update Changelog"}
                               applyButton={<UiLoadableButton label={"Update Changelog"} onClick={handleSubmit} className={"mx-0"}>Update Changelog</UiLoadableButton>}>
        <div className={"mt-2 mb-1"}>
            <UiFormLabel>Title</UiFormLabel>
            <UiClickableTip id={"ideaTitle"} title={"Writing a Title"} description={"Keep longer than 10 and shorter than 70 characters."}/>
            <UiCol xs={12} className={"d-inline-block px-0"}>
                <UiCol xs={12} className={"pr-sm-0 pr-2 px-0 d-inline-block"}>
                    <UiFormControl minLength={10} maxLength={70} rows={1} type={"text"} defaultValue={title} placeholder={"Brief and descriptive title."} id={"titleTextarea"}
                                   onChange={e => {
                                       formatRemainingCharacters("remainingTitle", "titleTextarea", 70);
                                       setTitle(e.target.value.substring(0, 70));
                                   }} label={"Idea title"}/>
                </UiCol>
            </UiCol>
            <small className={"d-inline mt-1 float-left text-black-60"} id={"remainingTitle"}>
                70 Remaining
            </small>
        </div>
        <br/>
        <div className={"my-2"}>
            <UiFormLabel>Description</UiFormLabel>
            <UiClickableTip id={"ideaDescription"} title={"Writing a Description"} description={<React.Fragment>
                Write a detailed description of your feedback suggestion.<br/>
                Supports <strong>**basic markdown**</strong> <em>*elements*</em>.<br/>
                Please keep under 1800 characters.
            </React.Fragment>} aria-label={"Idea description"}/>
            <UiMarkdownFormControl label={"Write description"} as={TextareaAutosize} defaultValue={changelog.description} id={"descriptionTextarea"} rows={5} maxRows={10}
                                   placeholder={"Detailed and meaningful description."} minLength={10} maxLength={1800} required
                                   style={{resize: "none", overflow: "hidden"}}
                                   onChange={e => {
                                       e.target.value = e.target.value.substring(0, 1800);
                                       formatRemainingCharacters("remainingDescription", "descriptionTextarea", 1800);
                                   }}/>
            <small className={"d-inline mt-1 float-left text-black-60"} id={"remainingDescription"}>
                1800 Remaining
            </small>
            <small className={"d-inline mt-1 float-right text-black-60"}>
                Markdown Supported
            </small>
        </div>
    </UiDismissibleModal>
};

export default ChangelogUpdateModal;