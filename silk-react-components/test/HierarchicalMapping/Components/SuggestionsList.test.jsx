import React from 'react';
import {expect, assert} from 'chai';
import { mount } from 'enzyme';
import sinon from "sinon";
import chai from "chai";
import chaiEnzyme from "chai-enzyme";
import Enzyme from "enzyme/build";
import Adapter from "enzyme-adapter-react-15/build";
import SuggestionsList from '../../../src/HierarchicalMapping/Components/SuggestionsList';
import './SuggestionsList.server';
// Required for the store to work!
import SilkStore from './../../../src/SilkStore/silkStore';
import waitUntilReady from '../../test_helper'

chai.use(chaiEnzyme());
Enzyme.configure({ adapter: new Adapter() });

const selectors = {
    "cancel": "button.ecc-hm-suggestions-cancel",
    "loader" : "div.mdl-spinner",
    "errors": "div.ecc-hm-suggestions-error",
    "errorsContainer": "div.mdl-alert--danger",
    "suggestions": ".ecc-silk-mapping__suggestionlist .mdl-list .ecc-silk-mapping__ruleitem--literal"
}

const mountSuggestionsList = (testCase1, testCase2) => mount(
    <SuggestionsList
        onClose={() => {}}
        parent={{}}
        ruleId={testCase1}
        targetClassUris={[testCase2]}
    />
);

describe('SuggestionsList render with correct responses ( both 200 )', () => {

    // set spy on component did mount to check how oft it is called
    sinon.spy(SuggestionsList.prototype, 'componentDidMount');
    // mount the SuggestionsList
    const component = mountSuggestionsList("200", "200");

    it('mounts once', async () => {
        await waitUntilReady(component);
        expect(SuggestionsList.prototype.componentDidMount.calledOnce);
    });

    it('has a cancel button', () => {
        const cancelButton = component.find(selectors.cancel);
        expect(cancelButton).to.have.lengthOf(1);
    });

    it('does not load anymore', () => {
        const loadingSpinner = component.find(selectors.loader);
        expect(loadingSpinner).to.have.lengthOf(0);
    });

    it('contains 4 elements', () => {
        // The list show 4 elements
        const suggestionsList = component.find(selectors.suggestions);
        expect(suggestionsList).to.have.lengthOf(4);
    });

});

describe('SuggestionsList render with wrong responses ( both 404 mit errors )', () => {

    const component = mountSuggestionsList("404", "404");

    it('mounts', async () => {
        await waitUntilReady(component);
    });

    it('contains two error message boxes', () => {
        expect(component.find(selectors.errors)).to.have.lengthOf(2);
        expect(component.find(selectors.errorsContainer)).to.have.lengthOf(1);
    })
});

describe('SuggestionsList render with wrong responses ( 404 {title: "Not Found", detail: "Not found"}), 404 {title: "Not Found", detail: "Not found"}', () => {

    const component = mountSuggestionsList("404NF", "404NF");

    it('mounts', async () => {
        await waitUntilReady(component);
        // an update is required to ensure the last render is called
        component.update();
    });

    it('contains one error message box with 1 error', () => {
        expect(component.find(selectors.errors)).to.have.lengthOf(1);
        expect(component.find(selectors.errorsContainer)).to.have.lengthOf(1);
    })
});


describe('SuggestionsList render with wrong responses ( both 500 )', () => {

    const component = mountSuggestionsList("500", "500");

    it('mounts', async () => {
        // wait until the component having time to start doing promises
        await waitUntilReady(component);
    });

    it('contains 1 error box with 2 errors', () => {
        expect(component.find(selectors.errors)).to.have.lengthOf(2);
        expect(component.find(selectors.errorsContainer)).to.have.lengthOf(1);

        const errorStrings = [
            "first error title",
            "second error title",
        ];

        component.find(selectors.errors).forEach((node, id) => {
            expect(node.text()).to.contain(errorStrings[id]);
        });

    })
});
