import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';

import '@vaadin/details';
import 'qui-badge';

export class AppinfoConfigSources extends QwcHotReloadElement {
    jsonRpc = new JsonRpc(this);

    static styles = css`
      .heading { width: 100em; padding: 15px; background: var(--lumo-contrast-5pct); border-bottom: 1px solid var(--lumo-contrast-10pct); }
      .heading-details { display: flex; gap:10px; padding-top: 10px; font-size: 0.8em; }
      .property { width: 100em; text-align: left; padding-left: 20px; padding-right: 20px; color: var(--lumo-contrast-70pct); }
      .props { color: var(--lumo-primary-text-color); }
    `;

    static properties = {
        _configsources: {state: true}
    };

    constructor() {
        super();
        this._configsources = null;
    }

    connectedCallback() {
        super.connectedCallback();
        this.hotReload();
    }

    render() {
        if(this._configsources) {
            return html`${this._configsources.map(cs=>{
                return html`${this._renderConfigSource(cs)}`;
            })}`;
        } else {
            return html`<span>Loading Config Sources...</span>`;
        }
    }

    _renderConfigSource(cs) {
        return html`
            <vaadin-details theme="reverse">
                <div class="heading" slot="summary">
                    <qui-badge level='success'><span>${cs.name}</span></qui-badge>
                    <div class="heading-details">
                        <code class="props">${cs.size} Properties</code> <code>Ordinal ${cs.ordinal}</code>
                    </div>
                </div>

                <div class="properties">
                    ${this._renderProperties(cs.properties)}
                </div>
            </vaadin-details>`;
    }

    _renderProperties(props){
        const propsTemplates = [];

        props.forEach(property => {
            propsTemplates.push(html`<div class="property">${property.key} = <qui-badge level='contrast'>${property.value}</qui-badge></div>`);
        })

        return html`${propsTemplates}`;
    }

    hotReload(){
        this._configsources = null;
        this.jsonRpc.getAll().then(response => {
            this._configsources = response.result;
        });
    }
}
customElements.define('appinfo-config-sources', AppinfoConfigSources);